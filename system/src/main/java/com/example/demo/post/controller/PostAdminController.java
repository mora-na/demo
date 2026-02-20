package com.example.demo.post.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.demo.common.model.CommonResult;
import com.example.demo.common.web.BaseController;
import com.example.demo.common.web.permission.RequirePermission;
import com.example.demo.dept.service.DeptService;
import com.example.demo.post.config.PostConstants;
import com.example.demo.post.dto.PostCreateRequest;
import com.example.demo.post.dto.PostStatusRequest;
import com.example.demo.post.dto.PostUpdateRequest;
import com.example.demo.post.dto.PostVO;
import com.example.demo.post.entity.SysPost;
import com.example.demo.post.entity.UserPost;
import com.example.demo.post.service.PostService;
import com.example.demo.post.service.UserPostService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 岗位管理后台接口，提供岗位的查询、创建、更新与状态控制。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
@Validated
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostAdminController extends BaseController {

    private final PostService postService;
    private final UserPostService userPostService;
    private final DeptService deptService;
    private final PostConstants postConstants;

    /**
     * 获取岗位列表。
     *
     * @return 岗位列表
     */
    @GetMapping
    @RequirePermission("post:query")
    public CommonResult<List<PostVO>> list() {
        return success(toVOs(postService.list()));
    }

    /**
     * 查询岗位详情。
     *
     * @param id 岗位 ID
     * @return 岗位详情
     */
    @GetMapping("/{id}")
    @RequirePermission("post:query")
    public CommonResult<PostVO> detail(@PathVariable Long id) {
        SysPost post = postService.getById(id);
        if (post == null) {
            return error(postConstants.getController().getNotFoundCode(),
                    i18n(postConstants.getMessage().getPostNotFound()));
        }
        return success(toVO(post));
    }

    /**
     * 创建岗位。
     *
     * @param request 创建请求
     * @return 创建后的岗位信息
     */
    @PostMapping
    @RequirePermission("post:create")
    public CommonResult<PostVO> create(@Valid @RequestBody PostCreateRequest request) {
        if (existsCode(request.getCode(), null)) {
            return error(postConstants.getController().getBadRequestCode(),
                    i18n(postConstants.getMessage().getPostCodeExists()));
        }
        if (request.getDeptId() == null || deptService.getById(request.getDeptId()) == null) {
            return error(postConstants.getController().getBadRequestCode(),
                    i18n(postConstants.getMessage().getDeptNotFound()));
        }
        SysPost post = new SysPost();
        post.setName(request.getName());
        post.setCode(request.getCode());
        post.setDeptId(request.getDeptId());
        post.setStatus(normalizeStatus(request.getStatus()));
        post.setSort(request.getSort() == null ? postConstants.getSort().getDefaultSort() : request.getSort());
        post.setRemark(request.getRemark());
        postService.save(post);
        return success(toVO(post));
    }

    /**
     * 更新岗位基础信息。
     *
     * @param id      岗位 ID
     * @param request 更新请求
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @RequirePermission("post:update")
    public CommonResult<Void> update(@PathVariable Long id, @Valid @RequestBody PostUpdateRequest request) {
        SysPost existing = postService.getById(id);
        if (existing == null) {
            return error(postConstants.getController().getNotFoundCode(),
                    i18n(postConstants.getMessage().getPostNotFound()));
        }
        if (existsCode(request.getCode(), id)) {
            return error(postConstants.getController().getBadRequestCode(),
                    i18n(postConstants.getMessage().getPostCodeExists()));
        }
        if (request.getDeptId() != null && deptService.getById(request.getDeptId()) == null) {
            return error(postConstants.getController().getBadRequestCode(),
                    i18n(postConstants.getMessage().getDeptNotFound()));
        }
        com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<SysPost> update =
                Wrappers.lambdaUpdate(SysPost.class).eq(SysPost::getId, id);
        boolean changed = false;
        if (request.getName() != null) {
            update.set(SysPost::getName, request.getName());
            changed = true;
        }
        if (request.getCode() != null) {
            update.set(SysPost::getCode, request.getCode());
            changed = true;
        }
        if (request.getDeptId() != null) {
            update.set(SysPost::getDeptId, request.getDeptId());
            changed = true;
        }
        if (request.getStatus() != null) {
            update.set(SysPost::getStatus, request.getStatus());
            changed = true;
        }
        if (request.getSort() != null) {
            update.set(SysPost::getSort, request.getSort());
            changed = true;
        }
        if (request.getRemark() != null) {
            update.set(SysPost::getRemark, request.getRemark());
            changed = true;
        }
        if (changed && !postService.update(update)) {
            return error(postConstants.getController().getInternalServerErrorCode(),
                    i18n(postConstants.getMessage().getCommonUpdateFailed()));
        }
        return success();
    }

    /**
     * 更新岗位启用状态。
     *
     * @param id      岗位 ID
     * @param request 状态请求
     * @return 更新结果
     */
    @PutMapping("/{id}/status")
    @RequirePermission("post:disable")
    public CommonResult<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody PostStatusRequest request) {
        SysPost existing = postService.getById(id);
        if (existing == null) {
            return error(postConstants.getController().getNotFoundCode(),
                    i18n(postConstants.getMessage().getPostNotFound()));
        }
        Integer status = request.getStatus();
        if (notValidStatus(status)) {
            return error(postConstants.getController().getBadRequestCode(),
                    i18n(postConstants.getMessage().getCommonStatusInvalid()));
        }
        if (!postService.updateStatus(id, status)) {
            return error(postConstants.getController().getInternalServerErrorCode(),
                    i18n(postConstants.getMessage().getCommonStatusUpdateFailed()));
        }
        return success();
    }

    @DeleteMapping("/{id}")
    @RequirePermission("post:delete")
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Void> delete(@PathVariable Long id) {
        if (postService.getById(id) == null) {
            return error(postConstants.getController().getNotFoundCode(),
                    i18n(postConstants.getMessage().getPostNotFound()));
        }
        userPostService.remove(Wrappers.lambdaQuery(UserPost.class).eq(UserPost::getPostId, id));
        if (!postService.removeById(id)) {
            return error(postConstants.getController().getInternalServerErrorCode(),
                    i18n(postConstants.getMessage().getCommonDeleteFailed()));
        }
        return success();
    }

    @PostMapping("/batch-delete")
    @RequirePermission("post:delete")
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Void> batchDelete(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return success();
        }
        List<Long> uniqueIds = ids.stream()
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (uniqueIds.isEmpty()) {
            return success();
        }
        userPostService.remove(Wrappers.lambdaQuery(UserPost.class).in(UserPost::getPostId, uniqueIds));
        if (!postService.removeByIds(uniqueIds)) {
            return error(postConstants.getController().getInternalServerErrorCode(),
                    i18n(postConstants.getMessage().getCommonDeleteFailed()));
        }
        return success();
    }

    /**
     * 规范化岗位状态，非法值默认回退为启用。
     *
     * @param status 状态值
     * @return 规范化后的状态
     */
    private Integer normalizeStatus(Integer status) {
        if (notValidStatus(status)) {
            return postConstants.getStatus().getEnabled();
        }
        return status;
    }

    /**
     * 判断状态值是否合法。
     *
     * @param status 状态值
     * @return true 表示非法
     */
    private boolean notValidStatus(Integer status) {
        return status == null
                || (status != postConstants.getStatus().getDisabled()
                && status != postConstants.getStatus().getEnabled());
    }

    private List<PostVO> toVOs(List<SysPost> posts) {
        if (posts == null || posts.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return posts.stream().map(this::toVO).collect(Collectors.toList());
    }

    private PostVO toVO(SysPost post) {
        if (post == null) {
            return new PostVO();
        }
        PostVO vo = new PostVO();
        vo.setId(post.getId());
        vo.setName(post.getName());
        vo.setCode(post.getCode());
        vo.setDeptId(post.getDeptId());
        vo.setStatus(post.getStatus());
        vo.setSort(post.getSort());
        vo.setRemark(post.getRemark());
        return vo;
    }

    private boolean existsCode(String code, Long excludeId) {
        if (StringUtils.isBlank(code)) {
            return false;
        }
        SysPost one = postService.getOne(Wrappers.lambdaQuery(SysPost.class).eq(SysPost::getCode, code)
                .ne(excludeId != null, SysPost::getId, excludeId));
        return one != null;
    }
}
