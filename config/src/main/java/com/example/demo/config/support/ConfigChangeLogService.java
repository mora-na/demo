package com.example.demo.config.support;

import com.example.demo.config.entity.SysConfig;
import com.example.demo.config.entity.SysConfigChangeLog;
import com.example.demo.config.mapper.SysConfigChangeLogMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 配置变更流水服务。
 */
@Component
public class ConfigChangeLogService {

    public static final String TYPE_CREATE = "CREATE";
    public static final String TYPE_UPDATE = "UPDATE";
    public static final String TYPE_DELETE = "DELETE";

    private final SysConfigChangeLogMapper mapper;

    public ConfigChangeLogService(SysConfigChangeLogMapper mapper) {
        this.mapper = mapper;
    }

    public void record(SysConfig before,
                       SysConfig after,
                       String oldValue,
                       String newValue,
                       String changeType,
                       String nodeId) {
        SysConfig base = after != null ? after : before;
        if (base == null || mapper == null) {
            return;
        }
        SysConfigChangeLog log = new SysConfigChangeLog();
        log.setConfigId(base.getId());
        log.setConfigKey(base.getConfigKey());
        log.setConfigGroup(base.getConfigGroup());
        log.setConfigValueOld(oldValue);
        log.setConfigValueNew(newValue);
        log.setConfigType(base.getConfigType());
        log.setConfigVersion(base.getConfigVersion());
        log.setHotUpdate(base.getHotUpdate());
        log.setConfigSensitive(base.getConfigSensitive());
        log.setChangeType(changeType);
        log.setChangeTime(LocalDateTime.now());
        log.setNodeId(nodeId);
        log.setOperatorId(base.getUpdateBy());
        mapper.insert(log);
    }

    public List<SysConfigChangeLog> listAfterId(long lastId, int limit) {
        if (mapper == null) {
            return Collections.emptyList();
        }
        int safeLimit = Math.max(1, limit);
        return mapper.selectAfterId(lastId, safeLimit);
    }
}
