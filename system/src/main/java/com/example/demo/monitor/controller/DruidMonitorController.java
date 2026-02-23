package com.example.demo.monitor.controller;

import com.example.demo.auth.model.AuthContext;
import com.example.demo.auth.model.AuthUser;
import com.example.demo.common.model.CommonResult;
import com.example.demo.common.web.BaseController;
import com.example.demo.common.web.permission.PermissionProperties;
import com.example.demo.common.web.permission.RequirePermission;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Druid 数据源监控接口。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/23
 */
@RestController
@RequestMapping("/monitor/druid")
@RequiredArgsConstructor
public class DruidMonitorController extends BaseController {

    private static final String FACADE_CLASS = "com.alibaba.druid.stat.DruidStatManagerFacade";
    private static final String WEB_APP_MANAGER_CLASS = "com.alibaba.druid.support.http.stat.WebAppStatManager";
    private static final String SPRING_STAT_MANAGER_CLASS = "com.alibaba.druid.support.spring.stat.SpringStatManager";
    private static final int SQL_LIMIT = 200;

    private final PermissionProperties permissionProperties;

    @GetMapping("/summary")
    @RequirePermission("druid:monitor")
    public CommonResult<Map<String, Object>> summary() {
        if (!isSuperUser(AuthContext.get())) {
            return error(HttpStatus.FORBIDDEN.value(), i18n("auth.permission.denied"));
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("generatedAt", LocalDateTime.now().toString());
        Object facade = resolveFacade();
        if (facade == null) {
            payload.put("available", false);
            payload.put("datasources", Collections.emptyList());
            payload.put("datasourceDetails", Collections.emptyList());
            payload.put("sqls", Collections.emptyList());
            payload.put("sqlDetails", Collections.emptyList());
            payload.put("wall", Collections.emptyMap());
            payload.put("webapp", Collections.emptyList());
            payload.put("weburi", Collections.emptyList());
            payload.put("spring", Collections.emptyList());
            payload.put("session", Collections.emptyList());
            return success(payload);
        }
        payload.put("available", true);
        payload.put("basic", resolveBasicStats(facade));
        List<Object> datasources = ensureList(invokeNoArgs(facade, "getDataSourceStatDataList"));
        List<Object> sqls = ensureList(resolveSqlStats(facade, datasources));
        payload.put("datasources", datasources);
        payload.put("datasourceDetails", ensureList(resolveDataSourceDetails(facade, datasources)));
        payload.put("activeConnectionStacks", resolveActiveConnectionStacks(facade, datasources));
        payload.put("poolingConnectionInfo", resolvePoolingConnectionInfo(facade, datasources));
        payload.put("sqls", sqls);
        payload.put("sqlDetails", ensureList(resolveSqlDetails(facade, sqls)));
        payload.put("wall", resolveWallStats(facade, datasources));
        payload.put("webapp", ensureList(resolveWebAppStats()));
        payload.put("weburi", ensureList(resolveWebUriStats()));
        payload.put("spring", ensureList(resolveSpringStats()));
        payload.put("session", ensureList(resolveSessionStats()));
        return success(payload);
    }

    private Object resolveFacade() {
        try {
            Class<?> facadeClass = Class.forName(FACADE_CLASS);
            Method getInstance = facadeClass.getMethod("getInstance");
            return getInstance.invoke(null);
        } catch (Exception ex) {
            return null;
        }
    }

    private Object resolveSqlStats(Object facade, List<Object> datasources) {
        if (facade == null || datasources == null || datasources.isEmpty()) {
            return Collections.emptyList();
        }
        List<Object> merged = new ArrayList<>();
        for (Object item : datasources) {
            Integer id = extractIdentity(item);
            if (id == null) {
                continue;
            }
            Object result = invokeOneIntArg(facade, "getSqlStatDataList", id);
            List<Object> list = ensureList(result);
            if (!list.isEmpty()) {
                merged.addAll(list);
            }
        }
        return merged.isEmpty() ? Collections.emptyList() : merged;
    }

    private Object resolveWebUriStats() {
        Object manager = resolveManagerInstance(WEB_APP_MANAGER_CLASS);
        if (manager == null) {
            return Collections.emptyList();
        }
        Object result = invokeNoArgs(manager, "getURIStatData");
        if (result != null) {
            return result;
        }
        return Collections.emptyList();
    }

    private Map<String, Object> resolveBasicStats(Object facade) {
        Map<String, Object> basic = ensureMap(invokeNoArgs(facade, "returnJSONBasicStat"));
        if (basic.isEmpty()) {
            basic = new LinkedHashMap<>();
        }
        Object resetEnable = invokeNoArgs(facade, "isResetEnable");
        if (resetEnable != null && !basic.containsKey("ResetEnable")) {
            basic.put("ResetEnable", resetEnable);
        }
        Object resetCount = invokeNoArgs(facade, "getResetCount");
        if (resetCount != null && !basic.containsKey("ResetCount")) {
            basic.put("ResetCount", resetCount);
        }
        return basic;
    }

    private Map<String, Object> resolveActiveConnectionStacks(Object facade, List<Object> datasources) {
        if (facade == null || datasources == null || datasources.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Object> result = new LinkedHashMap<>();
        for (Object item : datasources) {
            Integer id = extractIdentity(item);
            if (id == null) {
                continue;
            }
            Object stacks = invokeOneIntArg(facade, "getActiveConnectionStackTraceByDataSourceId", id);
            if (stacks != null) {
                result.put(String.valueOf(id), stacks);
            }
        }
        return result;
    }

    private Map<String, Object> resolvePoolingConnectionInfo(Object facade, List<Object> datasources) {
        if (facade == null || datasources == null || datasources.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Object> result = new LinkedHashMap<>();
        for (Object item : datasources) {
            Integer id = extractIdentity(item);
            if (id == null) {
                continue;
            }
            Object info = invokeOneIntArg(facade, "getPoolingConnectionInfoByDataSourceId", id);
            if (info != null) {
                result.put(String.valueOf(id), info);
            }
        }
        return result;
    }

    private Object resolveSpringStats() {
        Object manager = resolveManagerInstance(SPRING_STAT_MANAGER_CLASS);
        if (manager == null) {
            return Collections.emptyList();
        }
        Object result = invokeNoArgs(manager, "getMethodStatData");
        if (result != null) {
            return result;
        }
        return Collections.emptyList();
    }

    private Object resolveWebAppStats() {
        Object manager = resolveManagerInstance(WEB_APP_MANAGER_CLASS);
        if (manager == null) {
            return Collections.emptyList();
        }
        Object result = invokeNoArgs(manager, "getWebAppStatData");
        if (result != null) {
            return result;
        }
        return Collections.emptyList();
    }

    private Object resolveSessionStats() {
        Object manager = resolveManagerInstance(WEB_APP_MANAGER_CLASS);
        if (manager == null) {
            return Collections.emptyList();
        }
        Object result = invokeNoArgs(manager, "getSessionStatData");
        if (result != null) {
            return result;
        }
        return Collections.emptyList();
    }

    private Object resolveDataSourceDetails(Object facade, List<Object> datasources) {
        if (facade == null || datasources == null || datasources.isEmpty()) {
            return Collections.emptyList();
        }
        List<Object> details = new ArrayList<>();
        for (Object item : datasources) {
            Integer id = extractIdentity(item);
            if (id == null) {
                continue;
            }
            Object detail = invokeOneIntArg(facade, "getDataSourceStatData", id);
            if (detail != null) {
                details.add(detail);
            }
        }
        return details;
    }

    private Object resolveSqlDetails(Object facade, List<Object> sqls) {
        if (facade == null || sqls == null || sqls.isEmpty()) {
            return Collections.emptyList();
        }
        List<Object> details = new ArrayList<>();
        for (Object item : sqls) {
            Integer id = extractIdentity(item);
            if (id == null) {
                continue;
            }
            Object detail = invokeOneIntArg(facade, "getSqlStatData", id);
            if (detail != null) {
                details.add(detail);
            }
        }
        return details;
    }

    private Object invokeNoArgs(Object target, String name) {
        if (target == null) {
            return null;
        }
        try {
            Method method = target.getClass().getMethod(name);
            return method.invoke(target);
        } catch (Exception ex) {
            return null;
        }
    }

    private Object invokeOneIntArg(Object target, String name, int value) {
        if (target == null) {
            return null;
        }
        try {
            Method method = target.getClass().getMethod(name, int.class);
            return method.invoke(target, value);
        } catch (Exception ex) {
            try {
                Method method = target.getClass().getMethod(name, Integer.class);
                return method.invoke(target, value);
            } catch (Exception inner) {
                return null;
            }
        }
    }

    private Map<String, Object> resolveWallStats(Object facade, List<Object> datasources) {
        if (facade == null || datasources == null || datasources.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Object> merged = null;
        for (Object item : datasources) {
            Integer id = extractIdentity(item);
            if (id == null) {
                continue;
            }
            Object value = invokeOneIntArg(facade, "getWallStatMap", id);
            Map<String, Object> wall = ensureMap(value);
            if (wall.isEmpty()) {
                continue;
            }
            if (merged == null) {
                merged = new LinkedHashMap<>(wall);
            } else {
                merged = mergeWallStats(facade, merged, wall);
            }
        }
        return merged == null ? Collections.emptyMap() : merged;
    }

    private Map<String, Object> mergeWallStats(Object facade, Map<String, Object> left, Map<String, Object> right) {
        if (left == null || left.isEmpty()) {
            return right == null ? Collections.emptyMap() : right;
        }
        if (right == null || right.isEmpty()) {
            return left;
        }
        try {
            Method method = facade.getClass().getMethod("mergeWallStat", Map.class, Map.class);
            Object merged = method.invoke(null, left, right);
            return ensureMap(merged);
        } catch (Exception ex) {
            return left;
        }
    }

    private Object resolveManagerInstance(String className) {
        try {
            Class<?> managerClass = Class.forName(className);
            Method method = managerClass.getMethod("getInstance");
            return method.invoke(null);
        } catch (Exception ex) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private List<Object> ensureList(Object value) {
        if (value instanceof List) {
            return (List<Object>) value;
        }
        if (value instanceof java.util.Collection) {
            return new ArrayList<>((java.util.Collection<Object>) value);
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> ensureMap(Object value) {
        if (value instanceof Map) {
            Map<String, Object> raw = (Map<String, Object>) value;
            if (raw.isEmpty()) {
                return Collections.emptyMap();
            }
            Map<String, Object> normalized = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : raw.entrySet()) {
                String key = entry.getKey() == null ? "" : entry.getKey();
                normalized.put(key.trim(), entry.getValue());
            }
            return normalized;
        }
        return Collections.emptyMap();
    }

    @SuppressWarnings("unchecked")
    private Integer extractIdentity(Object item) {
        if (item instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) item;
            Object value = map.get("Identity");
            if (value == null) {
                value = map.get("identity");
            }
            if (value == null) {
                value = map.get("ID");
            }
            if (value == null) {
                value = map.get("Id");
            }
            if (value == null) {
                value = map.get("id");
            }
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            if (value instanceof String) {
                try {
                    return Integer.parseInt(((String) value).trim());
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    private boolean isSuperUser(AuthUser user) {
        if (user == null) {
            return false;
        }
        List<String> superUsers = permissionProperties.getSuperUsers();
        if (superUsers == null || superUsers.isEmpty()) {
            return false;
        }
        String userName = user.getUserName();
        if (userName == null) {
            return false;
        }
        String normalized = userName.toLowerCase(Locale.ROOT);
        for (String superUser : superUsers) {
            if (superUser != null && normalized.equals(superUser.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }
}
