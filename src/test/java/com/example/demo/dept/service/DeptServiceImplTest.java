package com.example.demo.dept.service;

import com.example.demo.dept.entity.Dept;
import com.example.demo.dept.mapper.DeptMapper;
import com.example.demo.dept.service.impl.DeptServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DeptServiceImplTest {

    @Test
    void updateStatus_updatesById() {
        DeptServiceImpl service = new DeptServiceImpl();
        DeptMapper mapper = mock(DeptMapper.class);
        ReflectionTestUtils.setField(service, "baseMapper", mapper);

        when(mapper.updateById(any(Dept.class))).thenReturn(1);

        assertTrue(service.updateStatus(1L, 0));
    }

    @Test
    void updateStatus_returnsFalseWhenIdMissing() {
        DeptServiceImpl service = new DeptServiceImpl();
        assertFalse(service.updateStatus(null, 1));
    }
}
