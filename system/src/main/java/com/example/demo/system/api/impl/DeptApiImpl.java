package com.example.demo.system.api.impl;

import com.example.demo.dept.entity.Dept;
import com.example.demo.dept.service.DeptService;
import com.example.demo.system.api.dept.DeptApi;
import com.example.demo.system.api.dept.DeptDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeptApiImpl implements DeptApi {

    private final DeptService deptService;

    @Override
    public DeptDTO getById(Long id) {
        Dept dept = deptService.getById(id);
        if (dept == null) {
            return null;
        }
        DeptDTO dto = new DeptDTO();
        dto.setId(dept.getId());
        dto.setName(dept.getName());
        return dto;
    }
}
