package com.example.demo.post.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.post.entity.UserPost;
import com.example.demo.post.mapper.UserPostMapper;
import com.example.demo.post.service.UserPostService;
import org.springframework.stereotype.Service;

/**
 * 用户-岗位关联服务实现。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
@Service
public class UserPostServiceImpl extends ServiceImpl<UserPostMapper, UserPost> implements UserPostService {
}
