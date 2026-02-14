package com.example.demo.post.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.post.entity.SysPost;

/**
 * 岗位服务接口。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
public interface PostService extends IService<SysPost> {

    /**
     * 更新岗位启用状态。
     *
     * @param id     岗位 ID
     * @param status 状态
     * @return true 表示更新成功
     */
    boolean updateStatus(Long id, Integer status);
}
