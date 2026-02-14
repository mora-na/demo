package com.example.demo.common.mybatis;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.example.demo.auth.model.AuthContext;
import com.example.demo.auth.model.AuthUser;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 字段自动填充处理器。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/12
 */
@Component
public class AutoFillMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        String userKey = resolveUserKey();
        Long deptId = resolveDeptId();
        strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
        strictInsertFill(metaObject, "createBy", String.class, userKey);
        strictInsertFill(metaObject, "createDept", Long.class, deptId);
        strictInsertFill(metaObject, "updateBy", String.class, userKey);
        strictInsertFill(metaObject, "isDeleted", Integer.class, 0);
        strictInsertFill(metaObject, "version", Integer.class, 0);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        String userKey = resolveUserKey();
        strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, now);
        strictUpdateFill(metaObject, "updateBy", String.class, userKey);
    }

    private String resolveUserKey() {
        AuthUser user = AuthContext.get();
        if (user == null) {
            return null;
        }
        if (user.getId() != null) {
            return String.valueOf(user.getId());
        }
        return user.getUserName();
    }

    private Long resolveDeptId() {
        AuthUser user = AuthContext.get();
        if (user == null) {
            return null;
        }
        return user.getDeptId();
    }
}
