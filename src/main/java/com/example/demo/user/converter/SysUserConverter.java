package com.example.demo.user.converter;

import com.example.demo.order.dto.OrderVO;
import com.example.demo.user.dto.SysUserQuery;
import com.example.demo.user.dto.SysUserVO;
import com.example.demo.user.entity.SysUser;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户实体与视图对象转换器，封装导入导出及聚合场景下的转换逻辑。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Component
public class SysUserConverter {

    public SysUser toEntity(SysUserQuery query) {
        if (query == null) {
            return new SysUser();
        }
        SysUser user = new SysUser();
        user.setId(query.getId());
        user.setUserName(query.getUserName());
        user.setNickName(query.getNickName());
        user.setPhone(query.getPhone());
        user.setEmail(query.getEmail());
        user.setSex(query.getSex());
        user.setRemark(query.getRemark());
        user.setStatus(query.getStatus());
        user.setDeptId(query.getDeptId());
        return user;
    }

    public SysUser toEntity(SysUserVO userVO) {
        if (userVO == null) {
            return new SysUser();
        }
        SysUser user = new SysUser();
        user.setId(userVO.getId());
        user.setUserName(userVO.getUserName());
        user.setNickName(userVO.getNickName());
        user.setPhone(userVO.getPhone());
        user.setEmail(userVO.getEmail());
        user.setSex(userVO.getSex());
        user.setStatus(userVO.getStatus());
        user.setDeptId(userVO.getDeptId());
        user.setDataScopeType(userVO.getDataScopeType());
        user.setDataScopeValue(userVO.getDataScopeValue());
        user.setRemark(userVO.getRemark());
        return user;
    }

    public List<SysUser> toEntityList(List<SysUserVO> userVOList) {
        if (userVOList == null || userVOList.isEmpty()) {
            return Collections.emptyList();
        }
        return userVOList.stream().map(this::toEntity).collect(Collectors.toList());
    }

    public SysUserVO toView(SysUser user, List<OrderVO> orderVOList) {
        if (user == null) {
            return new SysUserVO();
        }
        SysUserVO view = new SysUserVO();
        view.setId(user.getId());
        view.setUserName(user.getUserName());
        view.setNickName(user.getNickName());
        view.setPhone(user.getPhone());
        view.setEmail(user.getEmail());
        view.setSex(user.getSex());
        view.setStatus(user.getStatus());
        view.setDeptId(user.getDeptId());
        view.setDataScopeType(user.getDataScopeType());
        view.setDataScopeValue(user.getDataScopeValue());
        view.setRemark(user.getRemark());
        view.setOrderVOS(orderVOList);
        return view;
    }
}
