package com.example.demo.common.cache.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.common.cache.CacheEntry;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 缓存表 Mapper。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
public interface CacheMapper extends BaseMapper<CacheEntry> {

    @Select("SELECT cache_key, cache_value, value_class, expire_at FROM sys_cache WHERE cache_key = #{key} FOR UPDATE")
    CacheEntry selectForUpdate(@Param("key") String key);

    @Select("SELECT cache_key FROM sys_cache ORDER BY CASE WHEN expire_at IS NULL THEN 1 ELSE 0 END, expire_at ASC, cache_key ASC LIMIT #{limit}")
    List<String> selectKeysForEviction(@Param("limit") int limit);

    @Delete("DELETE FROM sys_cache WHERE expire_at IS NOT NULL AND expire_at < #{now}")
    int deleteExpired(@Param("now") long now);
}
