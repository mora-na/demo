package com.example.demo.post.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.demo.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户-岗位关联实体，映射 sys_user_post 表。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "system.sys_user_post")
public class UserPost extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID。
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 岗位 ID。
     */
    @TableField("post_id")
    private Long postId;
}
