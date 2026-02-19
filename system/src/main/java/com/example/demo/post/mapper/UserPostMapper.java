package com.example.demo.post.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.post.entity.UserPost;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户-岗位关联数据访问层。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */


@Mapper
public interface UserPostMapper extends BaseMapper<UserPost> {
}
