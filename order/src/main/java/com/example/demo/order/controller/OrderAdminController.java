package com.example.demo.order.controller;

import com.example.demo.common.model.CommonResult;
import com.example.demo.common.model.PageResult;
import com.example.demo.common.web.BaseController;
import com.example.demo.common.web.permission.RequirePermission;
import com.example.demo.identity.api.dto.IdentityUserDTO;
import com.example.demo.identity.api.facade.IdentityQueryApi;
import com.example.demo.order.converter.OrderConverter;
import com.example.demo.order.dto.OrderQuery;
import com.example.demo.order.dto.OrderVO;
import com.example.demo.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 订单管理接口。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/14
 */
@Validated
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderAdminController extends BaseController {

    private final OrderService orderService;
    private final IdentityQueryApi identityQueryApi;
    private final OrderConverter orderConverter;

    @GetMapping
    @RequirePermission("order:query")
    public CommonResult<PageResult<OrderVO>> list(@ModelAttribute OrderQuery query) {
        if (query != null && query.getMinAmount() != null && query.getMaxAmount() != null
                && query.getMinAmount().compareTo(query.getMaxAmount()) > 0) {
            return error(400, i18n("order.amount.invalid"));
        }
        PageResult<OrderVO> result = page(query, orderService::selectOrdersPage, orderConverter::toView);
        fillUserInfo(result.getData());
        return success(result);
    }

    @DeleteMapping("/{id}")
    @RequirePermission("order:delete")
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Void> delete(@PathVariable Long id) {
        if (orderService.getById(id) == null) {
            return error(404, i18n("order.not.found"));
        }
        if (!orderService.removeById(id)) {
            return error(500, i18n("common.delete.failed"));
        }
        return success();
    }

    private boolean isValidAmount(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    private void fillUserInfo(List<OrderVO> views) {
        if (views == null || views.isEmpty()) {
            return;
        }
        List<Long> userIds = views.stream()
                .map(OrderVO::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (userIds.isEmpty()) {
            return;
        }
        Map<Long, IdentityUserDTO> userMap = identityQueryApi.listUsersByIds(userIds).stream()
                .filter(Objects::nonNull)
                .filter(user -> user.getId() != null)
                .collect(Collectors.toMap(IdentityUserDTO::getId, user -> user, (left, right) -> right));
        for (OrderVO view : views) {
            if (view == null || view.getUserId() == null) {
                continue;
            }
            IdentityUserDTO user = userMap.get(view.getUserId());
            if (user != null) {
                view.setUserName(user.getUserName());
                view.setNickName(user.getNickName());
            }
        }
    }
}
