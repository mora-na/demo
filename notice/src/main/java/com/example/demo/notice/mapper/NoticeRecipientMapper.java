package com.example.demo.notice.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.demo.common.spring.SpringContextHolder;
import com.example.demo.notice.config.NoticeConstants;
import com.example.demo.notice.dto.*;
import com.example.demo.notice.entity.NoticeRecipient;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 通知接收记录数据访问层。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/12
 */


@Mapper
public interface NoticeRecipientMapper extends BaseMapper<NoticeRecipient> {

    static NoticeConstants constants() {
        NoticeConstants bean = SpringContextHolder.getBean(NoticeConstants.class);
        return bean == null ? new NoticeConstants() : bean;
    }

    static Long toLong(Object value) {
        if (value == null) {
            return constants().getNumeric().getZeroLong();
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return constants().getNumeric().getZeroLong();
        }
    }

    static Object getFirst(Map<String, Object> row, String... keys) {
        if (row == null || row.isEmpty() || keys == null) {
            return null;
        }
        for (String key : keys) {
            if (key == null) {
                continue;
            }
            if (row.containsKey(key)) {
                return row.get(key);
            }
        }
        return null;
    }

    default List<NoticeReadStat> selectReadStats(List<Long> noticeIds) {
        if (noticeIds == null || noticeIds.isEmpty()) {
            return Collections.emptyList();
        }
        QueryWrapper<NoticeRecipient> wrapper = new QueryWrapper<>();
        wrapper.select(
                        "notice_id as noticeId",
                        "count(1) as totalCount",
                        "sum(case when read_status = " + constants().getRecipient().getRead() + " then 1 else 0 end) as readCount")
                .in("notice_id", noticeIds)
                .groupBy("notice_id");
        List<Map<String, Object>> rows = selectMaps(wrapper);
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        List<NoticeReadStat> stats = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            if (row == null || row.isEmpty()) {
                continue;
            }
            NoticeReadStat stat = new NoticeReadStat();
            stat.setNoticeId(toLong(getFirst(row, "noticeId", "noticeid", "notice_id", "NOTICE_ID", "NOTICEID")));
            stat.setTotalCount(toLong(getFirst(row, "totalCount", "totalcount", "total_count", "TOTAL_COUNT", "TOTALCOUNT")));
            stat.setReadCount(toLong(getFirst(row, "readCount", "readcount", "read_count", "READ_COUNT", "READCOUNT")));
            stats.add(stat);
        }
        return stats;
    }

    @Select("select nr.id as id, nr.user_id as userId, nr.read_status as readStatus, nr.read_time as readTime " +
            "from demo_notice.sys_notice_recipient nr " +
            "where nr.notice_id = #{noticeId} and nr.is_deleted = 0 " +
            "order by nr.read_status asc, nr.read_time desc, nr.user_id asc")
    List<NoticeRecipientVO> selectRecipientsByNoticeId(@Param("noticeId") Long noticeId);

    @Select("select n.id as id, n.title as title, n.content as content, " +
            "n.created_name as createdName, n.create_time as createdAt, " +
            "nr.read_status as readStatus, nr.read_time as readTime " +
            "from demo_notice.sys_notice n join demo_notice.sys_notice_recipient nr on nr.notice_id = n.id " +
            "where nr.user_id = #{userId} and n.is_deleted = 0 and nr.is_deleted = 0 " +
            "order by n.create_time desc, n.id desc")
    List<NoticeMyVO> selectMyNotices(@Param("userId") Long userId);

    @Select("select n.id as id, n.title as title, n.content as content, " +
            "n.created_name as createdName, n.create_time as createdAt, " +
            "nr.read_status as readStatus, nr.read_time as readTime " +
            "from demo_notice.sys_notice n join demo_notice.sys_notice_recipient nr on nr.notice_id = n.id " +
            "where nr.user_id = #{userId} and n.is_deleted = 0 and nr.is_deleted = 0 " +
            "order by n.create_time desc, n.id desc")
    IPage<NoticeMyVO> selectMyNoticesPage(IPage<NoticeMyVO> page, @Param("userId") Long userId);

    default Long countUnreadByUserId(@Param("userId") Long userId) {
        if (userId == null) {
            return constants().getNumeric().getZeroLong();
        }
        QueryWrapper<NoticeRecipient> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
                .eq("read_status", constants().getRecipient().getUnread())
                .eq("is_deleted", constants().getCommon().getNotDeletedFlag());
        Long count = selectCount(wrapper);
        return count == null ? constants().getNumeric().getZeroLong() : count;
    }

    @Select("select n.id as id, n.title as title, n.created_name as createdName, n.create_time as createdAt, " +
            "nr.read_status as readStatus, nr.read_time as readTime " +
            "from demo_notice.sys_notice n join demo_notice.sys_notice_recipient nr on nr.notice_id = n.id " +
            "where nr.user_id = #{userId} and n.is_deleted = 0 and nr.is_deleted = 0 " +
            "order by n.create_time desc, n.id desc " +
            "limit #{limit}")
    List<NoticeLatestVO> selectMyLatestNotices(@Param("userId") Long userId, @Param("limit") int limit);

    default List<NoticeUnreadCount> countUnreadByUserIds(@Param("userIds") List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }
        QueryWrapper<NoticeRecipient> wrapper = new QueryWrapper<>();
        wrapper.select("user_id as userId", "count(1) as unreadCount")
                .eq("is_deleted", constants().getCommon().getNotDeletedFlag())
                .eq("read_status", constants().getRecipient().getUnread())
                .in("user_id", userIds)
                .groupBy("user_id");
        List<Map<String, Object>> rows = selectMaps(wrapper);
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        List<NoticeUnreadCount> results = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            if (row == null || row.isEmpty()) {
                continue;
            }
            NoticeUnreadCount count = new NoticeUnreadCount();
            count.setUserId(toLong(getFirst(row, "userId", "userid", "user_id", "USER_ID", "USERID")));
            count.setUnreadCount(toLong(getFirst(row, "unreadCount", "unreadcount", "unread_count", "UNREAD_COUNT", "UNREADCOUNT")));
            results.add(count);
        }
        return results;
    }
}
