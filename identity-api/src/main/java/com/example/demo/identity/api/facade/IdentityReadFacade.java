package com.example.demo.identity.api.facade;

import com.example.demo.identity.api.dto.IdentityDataScopeProfileDTO;
import com.example.demo.identity.api.dto.IdentityMenuTreeDTO;
import com.example.demo.identity.api.dto.IdentityUserProfileUpdateRequest;

import java.util.List;

/**
 * 身份域读接口，供其他模块通过稳定契约读取身份数据。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/16
 */
public interface IdentityReadFacade extends IdentityQueryApi, IdentityRoleApi, OrgApi {

    boolean updateSelfProfile(Long userId, IdentityUserProfileUpdateRequest request, String newPassword);

    List<IdentityMenuTreeDTO> listMenusByUserId(Long userId);

    List<IdentityMenuTreeDTO> listActiveMenus();

    IdentityDataScopeProfileDTO buildDataScopeProfile(Long userId);
}
