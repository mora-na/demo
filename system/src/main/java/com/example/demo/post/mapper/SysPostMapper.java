package com.example.demo.post.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.post.entity.SysPost;
import org.apache.ibatis.annotations.Mapper;

/**
 * 岗位数据访问层，封装岗位表基础 CRUD 能力。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */


@Mapper
public interface SysPostMapper extends BaseMapper<SysPost> {
}
