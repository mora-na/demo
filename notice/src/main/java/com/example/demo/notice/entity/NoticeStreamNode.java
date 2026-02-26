package com.example.demo.notice.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * SSE 节点连接计数。
 */
@Data
@TableName("demo_notice.sys_notice_stream_node")
public class NoticeStreamNode implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("node_id")
    private String nodeId;

    @TableField("connection_count")
    private Long connectionCount;

    @TableField("heartbeat_at")
    private LocalDateTime heartbeatAt;
}
