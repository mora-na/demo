package com.example.demo.dict.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.demo.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 字典数据项实体。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("demo_dict.sys_dict_data")
public class DictData extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableField("dict_type")
    private String dictType;

    @TableField("dict_label")
    private String dictLabel;

    @TableField("dict_value")
    private String dictValue;

    @TableField("status")
    private Integer status;

    @TableField("sort")
    private Integer sort;
}
