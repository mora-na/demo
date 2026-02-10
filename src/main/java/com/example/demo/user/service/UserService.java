package com.example.demo.user.service;

import com.example.demo.common.mybatis.IMppService;
import com.example.demo.user.dto.UserCreateRequest;
import com.example.demo.user.dto.UserQuery;
import com.example.demo.user.dto.UserUpdateRequest;
import com.example.demo.user.entity.User;

import java.util.List;

/**
 * 用户服务接口，封装查询、创建、更新及权限相关的业务能力。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
public interface UserService extends IMppService<User> {

    List<User> selectUsers(UserQuery query);

    User getByUserName(String userName);

    User createUser(UserCreateRequest request);

    boolean updateUser(Long id, UserUpdateRequest request);

    boolean updateStatus(Long id, Integer status);

    boolean resetPassword(Long id, String newPassword);

    boolean assignRoles(Long id, List<Long> roleIds);

    boolean updateDataScope(Long id, String dataScopeType, String dataScopeValue);
}
