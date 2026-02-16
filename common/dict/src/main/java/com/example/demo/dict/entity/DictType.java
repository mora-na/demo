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
 * 字典类型实体。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("dict.sys_dict_type")
public class DictType extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableField("dict_type")
    private String dictType;

    @TableField("dict_name")
    private String dictName;

    @TableField("status")
    private Integer status;

    @TableField("sort")
    private Integer sort;
}
