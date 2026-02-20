package com.example.demo.identity.api.facade;

import com.example.demo.identity.api.dto.IdentityUserDTO;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * 身份查询接口，提供用户基础信息查询能力。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/16
 */
public interface IdentityQueryApi {

    @Nullable
    IdentityUserDTO getUserById(Long userId);

    @Nullable
    IdentityUserDTO getUserByUserName(String userName);

    List<IdentityUserDTO> listUsersByIds(Collection<Long> userIds);
}
