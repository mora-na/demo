package com.example.demo.common.cache.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.common.cache.CacheEntry;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 缓存表 Mapper。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */


@Mapper
public interface CacheMapper extends BaseMapper<CacheEntry> {

    @Select("SELECT cache_key, cache_value, value_class, expire_at FROM cache.sys_cache WHERE cache_key = #{key} FOR UPDATE")
    CacheEntry selectForUpdate(@Param("key") String key);

    @Insert("INSERT INTO cache.sys_cache (cache_key, cache_value, value_class, expire_at) " +
            "VALUES (#{cacheKey}, #{cacheValue}, #{valueClass}, #{expireAt}) " +
            "ON CONFLICT (cache_key) DO NOTHING")
    int insertIgnore(CacheEntry entry);

    @Update("UPDATE cache.sys_cache SET cache_value = #{entry.cacheValue}, value_class = #{entry.valueClass}, " +
            "expire_at = #{entry.expireAt} WHERE cache_key = #{entry.cacheKey} " +
            "AND expire_at IS NOT NULL AND expire_at < #{now}")
    int updateIfExpired(@Param("entry") CacheEntry entry, @Param("now") long now);

    @Select("SELECT cache_key FROM cache.sys_cache ORDER BY CASE WHEN expire_at IS NULL THEN 1 ELSE 0 END, expire_at ASC, cache_key ASC LIMIT #{limit}")
    List<String> selectKeysForEviction(@Param("limit") int limit);

    @Delete("DELETE FROM cache.sys_cache WHERE expire_at IS NOT NULL AND expire_at < #{now}")
    int deleteExpired(@Param("now") long now);

    default int deleteBatchIds(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return 0;
        }
        QueryWrapper<CacheEntry> wrapper = new QueryWrapper<>();
        wrapper.in("cache_key", keys);
        return delete(wrapper);
    }
}
