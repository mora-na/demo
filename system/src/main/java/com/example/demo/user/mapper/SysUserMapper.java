package com.example.demo.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.user.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户数据访问层，封装用户表的基础 CRUD 能力。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */


@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}
