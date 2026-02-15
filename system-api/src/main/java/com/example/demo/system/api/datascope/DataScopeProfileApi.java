package com.example.demo.system.api.datascope;

/**
 * 登录用户数据范围画像 API。
 */
public interface DataScopeProfileApi {

    DataScopeProfileDTO buildProfile(Long userId, Long deptId);
}
