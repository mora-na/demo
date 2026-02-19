package com.example.demo.dept.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.dept.entity.Dept;
import org.apache.ibatis.annotations.Mapper;

/**
 * 部门数据访问层。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */


@Mapper
public interface DeptMapper extends BaseMapper<Dept> {
}
