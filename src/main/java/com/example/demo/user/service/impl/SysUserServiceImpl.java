package com.example.demo.user.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.auth.dto.UserProfileUpdateRequest;
import com.example.demo.auth.service.PasswordService;
import com.example.demo.common.annotation.DataScope;
import com.example.demo.datascope.entity.UserDataScope;
import com.example.demo.datascope.service.UserDataScopeService;
import com.example.demo.permission.entity.UserRole;
import com.example.demo.permission.service.UserRoleService;
import com.example.demo.post.entity.UserPost;
import com.example.demo.post.service.UserPostService;
import com.example.demo.user.config.UserConstants;
import com.example.demo.user.converter.SysUserConverter;
import com.example.demo.user.dto.SysUserCreateRequest;
import com.example.demo.user.dto.SysUserQuery;
import com.example.demo.user.dto.SysUserUpdateRequest;
import com.example.demo.user.entity.SysUser;
import com.example.demo.user.mapper.SysUserMapper;
import com.example.demo.user.service.SysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final SysUserConverter userConverter;
    private final PasswordService passwordService;
    private final UserRoleService userRoleService;
    private final UserPostService userPostService;
    private final UserDataScopeService userDataScopeService;
    private final UserConstants userConstants;

    @Override
    @DataScope(permission = "user:query")
    public List<SysUser> selectUsers(SysUserQuery query) {
        return baseMapper.selectList(buildListQueryWrapper(query));
    }

    @Override
    @DataScope(permission = "user:query")
    public IPage<SysUser> selectUsersPage(Page<SysUser> page, SysUserQuery query) {
        if (page == null) {
            return new Page<>(userConstants.getPage().getDefaultPageNum(),
                    userConstants.getPage().getDefaultPageSize());
        }
        return baseMapper.selectPage(page, buildListQueryWrapper(query));
    }

    @Override
    @DataScope(permission = "user:query")
    public IPage<SysUser> searchUsersPage(Page<SysUser> page, String keyword) {
        if (page == null) {
            return new Page<>(userConstants.getPage().getDefaultPageNum(),
                    userConstants.getPage().getDefaultPageSize());
        }
        if (StringUtils.isBlank(keyword)) {
            return page;
        }
        return baseMapper.selectPage(page,
                Wrappers.lambdaQuery(SysUser.class)
                        .like(SysUser::getUserName, keyword)
                        .or()
                        .like(SysUser::getNickName, keyword));
    }

    @Override
    @DataScope(permission = "user:query")
    public SysUser getByIdScoped(Long id) {
        if (id == null) {
            return null;
        }
        return getById(id);
    }

    @Override
    @DataScope(permission = "user:query")
    public List<Long> listScopedUserIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        List<Long> uniqueIds = ids.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (uniqueIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return baseMapper.selectList(Wrappers.lambdaQuery(SysUser.class).in(SysUser::getId, uniqueIds))
                .stream()
                .map(SysUser::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser> buildListQueryWrapper(SysUserQuery query) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser> wrapper =
                Wrappers.lambdaQuery(SysUser.class);
        if (query == null) {
            return wrapper;
        }
        if (query.getId() != null) {
            wrapper.eq(SysUser::getId, query.getId());
        }
        if (StringUtils.isNotBlank(query.getUserName())) {
            wrapper.like(SysUser::getUserName, query.getUserName());
        }
        if (StringUtils.isNotBlank(query.getNickName())) {
            wrapper.like(SysUser::getNickName, query.getNickName());
        }
        if (StringUtils.isNotBlank(query.getPhone())) {
            wrapper.eq(SysUser::getPhone, query.getPhone());
        }
        if (StringUtils.isNotBlank(query.getEmail())) {
            wrapper.eq(SysUser::getEmail, query.getEmail());
        }
        if (StringUtils.isNotBlank(query.getSex())) {
            wrapper.eq(SysUser::getSex, query.getSex());
        }
        if (StringUtils.isNotBlank(query.getRemark())) {
            wrapper.like(SysUser::getRemark, query.getRemark());
        }
        if (query.getStatus() != null) {
            wrapper.eq(SysUser::getStatus, query.getStatus());
        }
        if (query.getDeptId() != null) {
            wrapper.eq(SysUser::getDeptId, query.getDeptId());
        }
        return wrapper;
    }

    @Override
    public SysUser getByUserName(String userName) {
        if (userName == null) {
            return null;
        }
        return baseMapper.selectOne(Wrappers.lambdaQuery(SysUser.class).eq(SysUser::getUserName, userName));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUser createUser(SysUserCreateRequest request) {
        if (request == null) {
            return null;
        }
        SysUser user = new SysUser();
        user.setUserName(request.getUserName());
        user.setNickName(request.getNickName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setSex(request.getSex());
        user.setRemark(request.getRemark());
        user.setStatus(request.getStatus() == null ? userConstants.getStatus().getEnabled() : request.getStatus());
        user.setDeptId(request.getDeptId());
        user.setDataScopeType(request.getDataScopeType());
        user.setDataScopeValue(request.getDataScopeValue());
        user.setPassword(passwordService.encode(request.getPassword()));
        save(user);
        return user;
    }

    @Override
    @DataScope(permission = "user:query")
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(Long id, SysUserUpdateRequest request) {
        if (id == null || request == null) {
            return false;
        }
        SysUser user = new SysUser();
        user.setId(id);
        user.setUserName(request.getUserName());
        user.setNickName(request.getNickName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setSex(request.getSex());
        user.setStatus(request.getStatus());
        user.setDeptId(request.getDeptId());
        user.setRemark(request.getRemark());
        return updateById(user);
    }

    @Override
    @DataScope(permission = "user:query")
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long id, Integer status) {
        if (id == null) {
            return false;
        }
        SysUser user = new SysUser();
        user.setId(id);
        user.setStatus(status);
        return updateById(user);
    }

    @Override
    @DataScope(permission = "user:query")
    @Transactional(rollbackFor = Exception.class)
    public boolean resetPassword(Long id, String newPassword) {
        if (id == null || newPassword == null) {
            return false;
        }
        SysUser user = new SysUser();
        user.setId(id);
        user.setPassword(passwordService.encode(newPassword));
        return updateById(user);
    }

    @Override
    @DataScope(permission = "user:query")
    @Transactional(rollbackFor = Exception.class)
    public boolean assignRoles(Long id, List<Long> roleIds) {
        if (id == null) {
            return false;
        }
        if (getById(id) == null) {
            return false;
        }
        userRoleService.remove(Wrappers.lambdaQuery(UserRole.class).eq(UserRole::getUserId, id));
        if (roleIds == null || roleIds.isEmpty()) {
            return true;
        }
        List<UserRole> relations = roleIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .map(roleId -> {
                    UserRole relation = new UserRole();
                    relation.setUserId(id);
                    relation.setRoleId(roleId);
                    return relation;
                })
                .collect(Collectors.toList());
        if (relations.isEmpty()) {
            return true;
        }
        return userRoleService.saveBatch(relations);
    }

    @Override
    @DataScope(permission = "user:query")
    @Transactional(rollbackFor = Exception.class)
    public boolean assignPosts(Long id, List<Long> postIds) {
        if (id == null) {
            return false;
        }
        if (getById(id) == null) {
            return false;
        }
        userPostService.remove(Wrappers.lambdaQuery(UserPost.class).eq(UserPost::getUserId, id));
        if (postIds == null || postIds.isEmpty()) {
            return true;
        }
        List<UserPost> relations = postIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .map(postId -> {
                    UserPost relation = new UserPost();
                    relation.setUserId(id);
                    relation.setPostId(postId);
                    return relation;
                })
                .collect(Collectors.toList());
        if (relations.isEmpty()) {
            return true;
        }
        return userPostService.saveBatch(relations);
    }

    @Override
    @DataScope(permission = "user:query")
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDataScope(Long id, String dataScopeType, String dataScopeValue, String scopeKey) {
        if (id == null) {
            return false;
        }
        if (getById(id) == null) {
            return false;
        }
        // 兼容旧字段：同步保存到 sys_user
        SysUser user = new SysUser();
        user.setId(id);
        user.setDataScopeType(dataScopeType);
        user.setDataScopeValue(dataScopeValue);
        boolean updated = updateById(user);

        String normalizedKey = (scopeKey == null || scopeKey.trim().isEmpty())
                ? userConstants.getScope().getGlobalScopeKey()
                : scopeKey.trim();
        UserDataScope record = userDataScopeService.getOne(
                com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery(UserDataScope.class)
                        .eq(UserDataScope::getUserId, id)
                        .eq(UserDataScope::getScopeKey, normalizedKey)
        );
        if (record == null) {
            record = new UserDataScope();
            record.setUserId(id);
            record.setScopeKey(normalizedKey);
            record.setStatus(userConstants.getStatus().getDataScopeEnabled());
        }
        record.setDataScopeType(dataScopeType);
        record.setDataScopeValue(dataScopeValue);
        if (record.getId() == null) {
            return updated && userDataScopeService.save(record);
        }
        return updated && userDataScopeService.updateById(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSelfProfile(Long id, UserProfileUpdateRequest request, String newPassword) {
        if (id == null || request == null) {
            return false;
        }
        SysUser user = new SysUser();
        user.setId(id);
        user.setNickName(request.getNickName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setSex(request.getSex());
        user.setRemark(request.getRemark());
        if (StringUtils.isNotBlank(newPassword)) {
            user.setPassword(passwordService.encode(newPassword));
        }
        return updateById(user);
    }

    @Override
    @DataScope(permission = "user:query")
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUserScoped(Long id) {
        if (id == null) {
            return false;
        }
        SysUser user = getById(id);
        if (user == null) {
            return false;
        }
        userRoleService.remove(Wrappers.lambdaQuery(UserRole.class).eq(UserRole::getUserId, id));
        userPostService.remove(Wrappers.lambdaQuery(UserPost.class).eq(UserPost::getUserId, id));
        return removeById(id);
    }

    @Override
    @DataScope(permission = "user:query")
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUsersScoped(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return true;
        }
        List<Long> uniqueIds = ids.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (uniqueIds.isEmpty()) {
            return true;
        }
        List<SysUser> users = baseMapper.selectList(Wrappers.lambdaQuery(SysUser.class).in(SysUser::getId, uniqueIds));
        if (users.size() != uniqueIds.size()) {
            return false;
        }
        List<Long> allowedIds = users.stream()
                .map(SysUser::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        userRoleService.remove(Wrappers.lambdaQuery(UserRole.class).in(UserRole::getUserId, allowedIds));
        userPostService.remove(Wrappers.lambdaQuery(UserPost.class).in(UserPost::getUserId, allowedIds));
        return removeByIds(allowedIds);
    }

}
