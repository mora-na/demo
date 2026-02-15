package com.example.demo.system.api.profile;

/**
 * 登录用户角色/权限/菜单聚合 API。
 */
public interface AuthProfileApi {

    AuthProfileDTO buildProfile(Long userId, boolean superUser);
}
