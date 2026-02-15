package com.example.demo.notice.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.demo.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知接收记录实体，映射 sys_notice_recipient 表。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_notice_recipient")
public class NoticeRecipient extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 通知 ID。
     */
    @TableField("notice_id")
    private Long noticeId;

    /**
     * 接收用户 ID。
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 阅读状态：0-未读，1-已读。
     */
    @TableField("read_status")
    private Integer readStatus;

    /**
     * 阅读时间。
     */
    @TableField("read_time")
    private LocalDateTime readTime;

}
