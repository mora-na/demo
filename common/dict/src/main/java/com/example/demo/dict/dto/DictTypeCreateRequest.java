package com.example.demo.dict.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 字典类型创建请求。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/14
 */
@Data
public class DictTypeCreateRequest {

    @NotBlank
    @Size(max = 64)
    private String dictType;

    @NotBlank
    @Size(max = 128)
    private String dictName;

    private Integer status;

    private Integer sort;

    @Size(max = 500)
    private String remark;
}
