package com.example.demo.post.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.post.entity.SysPost;
import com.example.demo.post.mapper.SysPostMapper;
import com.example.demo.post.service.PostService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 岗位服务实现。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
@Service
public class PostServiceImpl extends ServiceImpl<SysPostMapper, SysPost> implements PostService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long id, Integer status) {
        if (id == null) {
            return false;
        }
        SysPost post = new SysPost();
        post.setId(id);
        post.setStatus(status);
        return updateById(post);
    }
}
