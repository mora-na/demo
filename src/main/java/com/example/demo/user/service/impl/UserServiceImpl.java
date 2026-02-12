package com.example.demo.user.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.auth.dto.UserProfileUpdateRequest;
import com.example.demo.auth.service.PasswordService;
import com.example.demo.permission.entity.UserRole;
import com.example.demo.permission.service.UserRoleService;
import com.example.demo.user.converter.UserConverter;
import com.example.demo.user.dto.UserCreateRequest;
import com.example.demo.user.dto.UserQuery;
import com.example.demo.user.dto.UserUpdateRequest;
import com.example.demo.user.entity.User;
import com.example.demo.user.mapper.UserMapper;
import com.example.demo.user.service.UserService;
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
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserConverter userConverter;
    private final PasswordService passwordService;
    private final UserRoleService userRoleService;

    @Override
    public List<User> selectUsers(UserQuery query) {
        return baseMapper.selectList(Wrappers.query(userConverter.toEntity(query)));
    }

    @Override
    public User getByUserName(String userName) {
        if (userName == null) {
            return null;
        }
        return baseMapper.selectOne(Wrappers.lambdaQuery(User.class).eq(User::getUserName, userName));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User createUser(UserCreateRequest request) {
        if (request == null) {
            return null;
        }
        User user = new User();
        user.setUserName(request.getUserName());
        user.setNickName(request.getNickName());
        user.setSex(request.getSex());
        user.setTst(request.getTst());
        user.setStatus(request.getStatus() == null ? User.STATUS_ENABLED : request.getStatus());
        user.setDeptId(request.getDeptId());
        user.setDataScopeType(request.getDataScopeType());
        user.setDataScopeValue(request.getDataScopeValue());
        user.setPassword(passwordService.encode(request.getPassword()));
        save(user);
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(Long id, UserUpdateRequest request) {
        if (id == null || request == null) {
            return false;
        }
        User user = new User();
        user.setId(id);
        user.setUserName(request.getUserName());
        user.setNickName(request.getNickName());
        user.setSex(request.getSex());
        user.setStatus(request.getStatus());
        user.setDeptId(request.getDeptId());
        user.setTst(request.getTst());
        return updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long id, Integer status) {
        if (id == null) {
            return false;
        }
        User user = new User();
        user.setId(id);
        user.setStatus(status);
        return updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetPassword(Long id, String newPassword) {
        if (id == null || newPassword == null) {
            return false;
        }
        User user = new User();
        user.setId(id);
        user.setPassword(passwordService.encode(newPassword));
        return updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignRoles(Long id, List<Long> roleIds) {
        if (id == null) {
            return false;
        }
        userRoleService.remove(Wrappers.lambdaQuery(UserRole.class).eq(UserRole::getUserId, id));
        if (roleIds == null || roleIds.isEmpty()) {
            return true;
        }
        List<UserRole> relations = roleIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .map(roleId -> new UserRole(null, id, roleId))
                .collect(Collectors.toList());
        if (relations.isEmpty()) {
            return true;
        }
        return userRoleService.saveBatch(relations);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDataScope(Long id, String dataScopeType, String dataScopeValue) {
        if (id == null) {
            return false;
        }
        User user = new User();
        user.setId(id);
        user.setDataScopeType(dataScopeType);
        user.setDataScopeValue(dataScopeValue);
        return updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSelfProfile(Long id, UserProfileUpdateRequest request, String newPassword) {
        if (id == null || request == null) {
            return false;
        }
        User user = new User();
        user.setId(id);
        user.setNickName(request.getNickName());
        user.setSex(request.getSex());
        user.setTst(request.getTst());
        if (StringUtils.isNotBlank(newPassword)) {
            user.setPassword(passwordService.encode(newPassword));
        }
        return updateById(user);
    }

}
