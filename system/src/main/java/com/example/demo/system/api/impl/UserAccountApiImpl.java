package com.example.demo.system.api.impl;

import com.example.demo.system.api.user.UserAccountApi;
import com.example.demo.system.api.user.UserAccountDTO;
import com.example.demo.system.api.user.UserProfileUpdateCommand;
import com.example.demo.system.api.user.UserSimpleDTO;
import com.example.demo.user.dto.SysUserProfileUpdateRequest;
import com.example.demo.user.entity.SysUser;
import com.example.demo.user.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserAccountApiImpl implements UserAccountApi {

    private final SysUserService userService;

    @Override
    public UserAccountDTO getById(Long id) {
        return toAccount(userService.getById(id));
    }

    @Override
    public UserAccountDTO getByUserName(String userName) {
        return toAccount(userService.getByUserName(userName));
    }

    @Override
    public List<UserSimpleDTO> listSimpleByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        List<SysUser> users = userService.listByIds(ids);
        if (users == null || users.isEmpty()) {
            return Collections.emptyList();
        }
        return users.stream()
                .filter(Objects::nonNull)
                .map(this::toSimple)
                .collect(Collectors.toList());
    }

    @Override
    public boolean updateSelfProfile(Long id, UserProfileUpdateCommand command, String newPassword) {
        if (id == null || command == null) {
            return false;
        }
        SysUserProfileUpdateRequest request = new SysUserProfileUpdateRequest();
        request.setNickName(command.getNickName());
        request.setPhone(command.getPhone());
        request.setEmail(command.getEmail());
        request.setSex(command.getSex());
        request.setRemark(command.getRemark());
        return userService.updateSelfProfile(id, request, newPassword);
    }

    private UserSimpleDTO toSimple(SysUser user) {
        UserSimpleDTO dto = new UserSimpleDTO();
        dto.setId(user.getId());
        dto.setUserName(user.getUserName());
        dto.setNickName(user.getNickName());
        return dto;
    }

    private UserAccountDTO toAccount(SysUser user) {
        if (user == null) {
            return null;
        }
        UserAccountDTO dto = new UserAccountDTO();
        dto.setId(user.getId());
        dto.setUserName(user.getUserName());
        dto.setNickName(user.getNickName());
        dto.setPhone(user.getPhone());
        dto.setEmail(user.getEmail());
        dto.setPassword(user.getPassword());
        dto.setPasswordUpdatedAt(user.getPasswordUpdatedAt());
        dto.setForcePasswordChange(user.getForcePasswordChange());
        dto.setStatus(user.getStatus());
        dto.setDeptId(user.getDeptId());
        dto.setDataScopeType(user.getDataScopeType());
        dto.setDataScopeValue(user.getDataScopeValue());
        dto.setSex(user.getSex());
        dto.setRemark(user.getRemark());
        return dto;
    }
}
