package com.example.demo.notice.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.notice.dto.NoticeMyVO;
import com.example.demo.notice.dto.NoticeReadStat;
import com.example.demo.notice.dto.NoticeRecipientVO;
import com.example.demo.notice.entity.NoticeRecipient;
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
public interface NoticeRecipientMapper extends BaseMapper<NoticeRecipient> {

    static Long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return 0L;
        }
    }

    default List<NoticeReadStat> selectReadStats(List<Long> noticeIds) {
        if (noticeIds == null || noticeIds.isEmpty()) {
            return Collections.emptyList();
        }
        QueryWrapper<NoticeRecipient> wrapper = new QueryWrapper<>();
        wrapper.select("notice_id as noticeId", "count(1) as totalCount", "sum(case when read_status = 1 then 1 else 0 end) as readCount").in("notice_id", noticeIds).groupBy("notice_id");
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
            stat.setNoticeId(toLong(row.getOrDefault("noticeId", row.get("notice_id"))));
            stat.setTotalCount(toLong(row.getOrDefault("totalCount", row.get("totalcount"))));
            stat.setReadCount(toLong(row.getOrDefault("readCount", row.get("readcount"))));
            stats.add(stat);
        }
        return stats;
    }

    @Select("select nr.id as id, nr.user_id as userId, u.user_name as userName, u.nick_name as nickName, " + "u.dept_id as deptId, nr.read_status as readStatus, nr.read_time as readTime " + "from sys_notice_recipient nr left join sys_user u on u.id = nr.user_id " + "where nr.notice_id = #{noticeId} " + "order by nr.read_status asc, nr.read_time desc, nr.user_id asc")
    List<NoticeRecipientVO> selectRecipientsByNoticeId(@Param("noticeId") Long noticeId);

    @Select("select n.id as id, n.title as title, n.content as content, " + "n.created_name as createdName, n.created_at as createdAt, " + "nr.read_status as readStatus, nr.read_time as readTime " + "from sys_notice n join sys_notice_recipient nr on nr.notice_id = n.id " + "where nr.user_id = #{userId} " + "order by n.created_at desc, n.id desc")
    List<NoticeMyVO> selectMyNotices(@Param("userId") Long userId);

    @Select("select count(1) from sys_notice_recipient where user_id = #{userId} and read_status = 0")
    Long countUnreadByUserId(@Param("userId") Long userId);
}
