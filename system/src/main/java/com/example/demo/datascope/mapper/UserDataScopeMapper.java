package com.example.demo.datascope.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.datascope.entity.UserDataScope;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户数据范围覆盖数据访问层。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */


@Mapper
public interface UserDataScopeMapper extends BaseMapper<UserDataScope> {
}
