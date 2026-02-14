package com.example.demo.dict.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 字典数据项查询参数。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/14
 */
@Data
public class DictDataQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    private String dictType;

    private String dictLabel;

    private String dictValue;

    private Integer status;
}
