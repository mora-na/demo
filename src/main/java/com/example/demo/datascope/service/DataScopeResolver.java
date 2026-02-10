package com.example.demo.datascope.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.demo.common.mybatis.DataScopeProperties;
import com.example.demo.common.mybatis.DataScopeType;
import com.example.demo.dept.entity.Dept;
import com.example.demo.dept.service.DeptService;
import com.example.demo.permission.entity.Role;
import com.example.demo.permission.entity.UserRole;
import com.example.demo.permission.service.RoleService;
import com.example.demo.permission.service.UserRoleService;
import com.example.demo.user.entity.User;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据范围解析服务，根据用户角色与部门信息计算最终数据范围。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Service
public class DataScopeResolver {

    private final UserRoleService userRoleService;
    private final RoleService roleService;
    private final DeptService deptService;
    private final DataScopeProperties properties;

    public DataScopeResolver(UserRoleService userRoleService,
                             RoleService roleService,
                             DeptService deptService,
                             DataScopeProperties properties) {
        this.userRoleService = userRoleService;
        this.roleService = roleService;
        this.deptService = deptService;
        this.properties = properties;
    }

    /**
     * 解析用户的最终数据范围配置。
     *
     * @param user 用户实体
     * @return 数据范围结果
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public DataScopeResult resolve(User user) {
        String fallback = properties == null ? DataScopeType.ALL : properties.getDefaultType();
        if (user == null || user.getId() == null) {
            return new DataScopeResult(normalizeType(null, fallback), null);
        }
        List<UserRole> relations = userRoleService.list(Wrappers.lambdaQuery(UserRole.class)
                .eq(UserRole::getUserId, user.getId()));
        if (relations == null || relations.isEmpty()) {
            return fallbackToUser(user, fallback);
        }
        List<Long> roleIds = relations.stream()
                .map(UserRole::getRoleId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (roleIds.isEmpty()) {
            return fallbackToUser(user, fallback);
        }
        List<Role> roles = roleService.listByIds(roleIds);
        if (roles == null || roles.isEmpty()) {
            return fallbackToUser(user, fallback);
        }
        String bestType = null;
        int bestPriority = -1;
        Set<Long> customDeptIds = new LinkedHashSet<>();
        for (Role role : roles) {
            if (role == null) {
                continue;
            }
            Integer status = role.getStatus();
            if (status != null && status != 1) {
                continue;
            }
            String type = normalizeType(role.getDataScopeType(), null);
            if (type == null) {
                continue;
            }
            int priority = priorityOf(type);
            if (priority > bestPriority) {
                bestPriority = priority;
                bestType = type;
            }
            if (DataScopeType.CUSTOM_DEPT.equals(type) || DataScopeType.CUSTOM.equals(type)) {
                customDeptIds.addAll(parseDeptIds(role.getDataScopeValue()));
            }
        }
        if (bestType == null) {
            return fallbackToUser(user, fallback);
        }
        if (DataScopeType.ALL.equals(bestType)) {
            return new DataScopeResult(bestType, null);
        }
        if (DataScopeType.SELF.equals(bestType)) {
            return new DataScopeResult(bestType, null);
        }
        if (DataScopeType.DEPT.equals(bestType)) {
            if (user.getDeptId() == null) {
                return new DataScopeResult(DataScopeType.NONE, null);
            }
            return new DataScopeResult(bestType, user.getDeptId().toString());
        }
        if (DataScopeType.DEPT_AND_CHILD.equals(bestType)) {
            if (user.getDeptId() == null) {
                return new DataScopeResult(DataScopeType.NONE, null);
            }
            List<Long> deptIds = resolveDeptTreeIds(user.getDeptId());
            if (deptIds.isEmpty()) {
                return new DataScopeResult(DataScopeType.NONE, null);
            }
            return new DataScopeResult(bestType, joinIds(deptIds));
        }
        if (DataScopeType.CUSTOM_DEPT.equals(bestType) || DataScopeType.CUSTOM.equals(bestType)) {
            if (customDeptIds.isEmpty()) {
                return new DataScopeResult(DataScopeType.NONE, null);
            }
            return new DataScopeResult(DataScopeType.CUSTOM_DEPT, joinIds(customDeptIds));
        }
        if (DataScopeType.NONE.equals(bestType)) {
            return new DataScopeResult(bestType, null);
        }
        return new DataScopeResult(normalizeType(bestType, fallback), null);
    }

    private DataScopeResult fallbackToUser(User user, String fallback) {
        String type = normalizeType(user == null ? null : user.getDataScopeType(), fallback);
        String value = user == null ? null : user.getDataScopeValue();
        return new DataScopeResult(type, value);
    }

    private List<Long> resolveDeptTreeIds(Long rootId) {
        List<Dept> depts = deptService.list();
        if (depts == null || depts.isEmpty()) {
            return Collections.singletonList(rootId);
        }
        Map<Long, List<Long>> children = new HashMap<>();
        for (Dept dept : depts) {
            if (dept == null || dept.getId() == null) {
                continue;
            }
            Long parentId = dept.getParentId();
            if (parentId == null) {
                continue;
            }
            children.computeIfAbsent(parentId, key -> new ArrayList<>()).add(dept.getId());
        }
        LinkedHashSet<Long> results = new LinkedHashSet<>();
        Deque<Long> queue = new ArrayDeque<>();
        queue.add(rootId);
        while (!queue.isEmpty()) {
            Long current = queue.poll();
            if (current == null || results.contains(current)) {
                continue;
            }
            results.add(current);
            List<Long> next = children.get(current);
            if (next != null) {
                queue.addAll(next);
            }
        }
        return new ArrayList<>(results);
    }

    private List<Long> parseDeptIds(String value) {
        if (value == null || value.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String[] tokens = value.split(",");
        List<Long> ids = new ArrayList<>();
        for (String token : tokens) {
            if (token == null) {
                continue;
            }
            String trimmed = token.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (!isNumeric(trimmed)) {
                continue;
            }
            try {
                ids.add(Long.parseLong(trimmed));
            } catch (NumberFormatException ignore) {
            }
        }
        return ids;
    }

    private boolean isNumeric(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private String joinIds(Collection<Long> ids) {
        return ids.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    private int priorityOf(String type) {
        if (DataScopeType.ALL.equals(type)) {
            return 6;
        }
        if (DataScopeType.DEPT_AND_CHILD.equals(type)) {
            return 5;
        }
        if (DataScopeType.DEPT.equals(type)) {
            return 4;
        }
        if (DataScopeType.CUSTOM_DEPT.equals(type) || DataScopeType.CUSTOM.equals(type)) {
            return 3;
        }
        if (DataScopeType.SELF.equals(type)) {
            return 2;
        }
        if (DataScopeType.NONE.equals(type)) {
            return 1;
        }
        return 0;
    }

    private String normalizeType(String value, String fallback) {
        String candidate = value;
        if (candidate == null || candidate.trim().isEmpty()) {
            candidate = fallback;
        }
        if (candidate == null) {
            return null;
        }
        return candidate.trim().toUpperCase(Locale.ROOT);
    }

    /**
     * 数据范围解析结果。
     */
    public static final class DataScopeResult {
        private final String type;
        private final String value;

        public DataScopeResult(String type, String value) {
            this.type = type;
            this.value = value;
        }

        public String getType() {
            return type;
        }

        public String getValue() {
            return value;
        }
    }
}
