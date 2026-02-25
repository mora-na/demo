package com.example.demo.common.cache;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 数据库缓存条目。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Data
@TableName(value = "demo_cache.sys_cache")
public class CacheEntry {

    @TableId(value = "cache_key", type = IdType.INPUT)
    private String cacheKey;

    @TableField("cache_value")
    private String cacheValue;

    @TableField("value_class")
    private String valueClass;

    @TableField("expire_at")
    private Long expireAt;
}
