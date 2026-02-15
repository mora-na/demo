package com.example.demo.system.api.user;

import java.util.Collection;
import java.util.List;

/**
 * 用户跨域查询与自助修改 API。
 */
public interface UserAccountApi {

    UserAccountDTO getById(Long id);

    UserAccountDTO getByUserName(String userName);

    List<UserSimpleDTO> listSimpleByIds(Collection<Long> ids);

    boolean updateSelfProfile(Long id, UserProfileUpdateCommand command, String newPassword);
}
