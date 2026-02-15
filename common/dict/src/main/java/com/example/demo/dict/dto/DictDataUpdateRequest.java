package com.example.demo.dict.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 字典数据项更新请求。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/14
 */
@Data
public class DictDataUpdateRequest {

    @NotBlank
    @Size(max = 128)
    private String dictLabel;

    @NotBlank
    @Size(max = 128)
    private String dictValue;

    private Integer status;

    private Integer sort;

    @Size(max = 500)
    private String remark;
}
