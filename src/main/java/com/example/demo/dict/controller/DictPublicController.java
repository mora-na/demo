package com.example.demo.dict.controller;

import com.example.demo.common.model.CommonResult;
import com.example.demo.common.web.permission.RequireLogin;
import com.example.demo.dict.dto.DictDataVO;
import com.example.demo.dict.service.DictService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 字典公开接口（登录用户可用）。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/14
 */
@RequireLogin
@RestController
@RequestMapping("/dict/data")
@RequiredArgsConstructor
public class DictPublicController {

    private final DictService dictService;

    @GetMapping("/{dictType}")
    public CommonResult<List<DictDataVO>> getByType(@PathVariable String dictType) {
        return CommonResult.success("success", dictService.getDataByType(dictType));
    }

    @GetMapping("/batch")
    public CommonResult<Map<String, List<DictDataVO>>> batch(@RequestParam("types") String types) {
        if (StringUtils.isBlank(types)) {
            return CommonResult.success("success", Collections.emptyMap());
        }
        String[] segments = types.split(",");
        List<String> list = new ArrayList<>();
        for (String segment : segments) {
            if (StringUtils.isNotBlank(segment)) {
                list.add(segment.trim());
            }
        }
        return CommonResult.success("success", dictService.getDataByTypes(list));
    }

    @GetMapping("/all")
    public CommonResult<Map<String, List<DictDataVO>>> all() {
        return CommonResult.success("success", dictService.getAllEnabled());
    }
}
