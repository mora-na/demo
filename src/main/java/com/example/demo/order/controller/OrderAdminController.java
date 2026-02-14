package com.example.demo.order.controller;

import com.example.demo.common.model.CommonResult;
import com.example.demo.common.model.PageResult;
import com.example.demo.common.web.BaseController;
import com.example.demo.common.web.permission.RequirePermission;
import com.example.demo.order.converter.OrderConverter;
import com.example.demo.order.dto.OrderCreateRequest;
import com.example.demo.order.dto.OrderQuery;
import com.example.demo.order.dto.OrderUpdateRequest;
import com.example.demo.order.dto.OrderVO;
import com.example.demo.order.entity.Order;
import com.example.demo.order.service.OrderService;
import com.example.demo.user.entity.SysUser;
import com.example.demo.user.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.Collections;
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
    private final SysUserService userService;
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

    @PostMapping
    @RequirePermission("order:create")
    public CommonResult<OrderVO> create(@Valid @RequestBody OrderCreateRequest request) {
        if (request.getUserId() == null || userService.getById(request.getUserId()) == null) {
            return error(400, i18n("user.not.found"));
        }
        if (!isValidAmount(request.getAmount())) {
            return error(400, i18n("order.amount.invalid"));
        }
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setAmount(request.getAmount());
        order.setRemark(StringUtils.trimToNull(request.getRemark()));
        if (!orderService.save(order)) {
            return error(500, i18n("common.error"));
        }
        OrderVO view = orderConverter.toView(order);
        fillUserInfo(Collections.singletonList(view));
        return success(view);
    }

    @PutMapping("/{id}")
    @RequirePermission("order:update")
    public CommonResult<Void> update(@PathVariable Long id, @Valid @RequestBody OrderUpdateRequest request) {
        Order existing = orderService.getById(id);
        if (existing == null) {
            return error(404, i18n("order.not.found"));
        }
        if (request.getUserId() != null && userService.getById(request.getUserId()) == null) {
            return error(400, i18n("user.not.found"));
        }
        if (request.getAmount() != null && !isValidAmount(request.getAmount())) {
            return error(400, i18n("order.amount.invalid"));
        }
        Order update = new Order();
        update.setId(id);
        update.setUserId(request.getUserId());
        update.setAmount(request.getAmount());
        update.setRemark(StringUtils.trimToNull(request.getRemark()));
        if (!orderService.updateById(update)) {
            return error(500, i18n("common.update.failed"));
        }
        return success();
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
        Map<Long, SysUser> userMap = userService.listByIds(userIds).stream()
                .filter(Objects::nonNull)
                .filter(user -> user.getId() != null)
                .collect(Collectors.toMap(SysUser::getId, user -> user, (left, right) -> right));
        for (OrderVO view : views) {
            if (view == null || view.getUserId() == null) {
                continue;
            }
            SysUser user = userMap.get(view.getUserId());
            if (user != null) {
                view.setUserName(user.getUserName());
                view.setNickName(user.getNickName());
            }
        }
    }
}
