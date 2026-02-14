package com.example.demo.dict.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 字典类型视图对象。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/14
 */
@Data
public class DictTypeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String dictType;

    private String dictName;

    private Integer status;

    private Integer sort;

    private String remark;

    private LocalDateTime createTime;
}
