package com.example.demo.notice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.notice.entity.NoticeStreamNode;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;

/**
 * SSE 节点连接计数访问层。
 */
@Mapper
public interface NoticeStreamNodeMapper extends BaseMapper<NoticeStreamNode> {

    @Update("UPDATE demo_notice.sys_notice_stream_node " +
            "SET connection_count = #{count}, heartbeat_at = #{heartbeatAt} " +
            "WHERE node_id = #{nodeId}")
    int updateHeartbeat(@Param("nodeId") String nodeId,
                        @Param("count") long count,
                        @Param("heartbeatAt") LocalDateTime heartbeatAt);

    @Insert("INSERT INTO demo_notice.sys_notice_stream_node (node_id, connection_count, heartbeat_at) " +
            "VALUES (#{nodeId}, #{count}, #{heartbeatAt})")
    int insertHeartbeat(@Param("nodeId") String nodeId,
                        @Param("count") long count,
                        @Param("heartbeatAt") LocalDateTime heartbeatAt);

    @Select("SELECT COALESCE(SUM(connection_count), 0) FROM demo_notice.sys_notice_stream_node " +
            "WHERE heartbeat_at >= #{minHeartbeat}")
    Long sumActiveConnections(@Param("minHeartbeat") LocalDateTime minHeartbeat);
}
