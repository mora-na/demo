package com.example.demo.system.api.impl;

import com.example.demo.datascope.model.DataScopeProfile;
import com.example.demo.datascope.service.DataScopeProfileService;
import com.example.demo.system.api.datascope.DataScopeProfileApi;
import com.example.demo.system.api.datascope.DataScopeProfileDTO;
import com.example.demo.user.entity.SysUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class DataScopeProfileApiImpl implements DataScopeProfileApi {

    private final DataScopeProfileService dataScopeProfileService;

    @Override
    public DataScopeProfileDTO buildProfile(Long userId, Long deptId) {
        DataScopeProfileDTO dto = new DataScopeProfileDTO();
        if (userId == null) {
            return dto;
        }
        SysUser user = new SysUser();
        user.setId(userId);
        user.setDeptId(deptId);
        DataScopeProfile profile = dataScopeProfileService.buildProfile(user);
        if (profile == null) {
            return dto;
        }
        dto.setDeptTreeIds(profile.getDeptTreeIds() == null ? Collections.emptySet() : profile.getDeptTreeIds());
        dto.setRoleDataScopes(profile.getRoleDataScopes() == null ? Collections.emptyList() : profile.getRoleDataScopes());
        dto.setUserScopeOverrides(profile.getUserScopeOverrides() == null ? Collections.emptyMap() : profile.getUserScopeOverrides());
        return dto;
    }
}
