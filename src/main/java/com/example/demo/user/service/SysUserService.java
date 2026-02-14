package com.example.demo.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.auth.dto.UserProfileUpdateRequest;
import com.example.demo.user.dto.SysUserCreateRequest;
import com.example.demo.user.dto.SysUserQuery;
import com.example.demo.user.dto.SysUserUpdateRequest;
import com.example.demo.user.entity.SysUser;

import java.util.List;

/**
 * 用户服务接口，封装查询、创建、更新及权限相关的业务能力。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
public interface SysUserService extends IService<SysUser> {

    List<SysUser> selectUsers(SysUserQuery query);

    IPage<SysUser> selectUsersPage(Page<SysUser> page, SysUserQuery query);

    SysUser getByUserName(String userName);

    SysUser createUser(SysUserCreateRequest request);

    boolean updateUser(Long id, SysUserUpdateRequest request);

    boolean updateStatus(Long id, Integer status);

    boolean resetPassword(Long id, String newPassword);

    boolean assignRoles(Long id, List<Long> roleIds);

    boolean assignPosts(Long id, List<Long> postIds);

    boolean updateDataScope(Long id, String dataScopeType, String dataScopeValue, String scopeKey);

    boolean updateSelfProfile(Long id, UserProfileUpdateRequest request, String newPassword);
}
