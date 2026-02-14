package com.example.demo.dict.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 字典数据项视图对象。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/14
 */
@Data
public class DictDataVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String dictType;

    private String dictLabel;

    private String dictValue;

    private Integer status;

    private Integer sort;

    private String remark;

    private LocalDateTime createTime;
}
