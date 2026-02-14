package com.example.demo.datascope.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.datascope.entity.UserDataScope;
import com.example.demo.datascope.mapper.UserDataScopeMapper;
import com.example.demo.datascope.service.UserDataScopeService;
import org.springframework.stereotype.Service;

/**
 * 用户数据范围覆盖服务实现。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
@Service
public class UserDataScopeServiceImpl extends ServiceImpl<UserDataScopeMapper, UserDataScope>
        implements UserDataScopeService {
}
