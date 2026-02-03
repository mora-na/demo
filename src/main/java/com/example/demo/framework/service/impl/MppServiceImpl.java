package com.example.demo.framework.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.example.demo.framework.service.IMppService;
import com.example.demo.framework.service.MppBaseMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MppServiceImpl<M extends MppBaseMapper<T>, T> extends com.github.jeffreyning.mybatisplus.service.MppServiceImpl<M, T> implements IMppService<T> {

    public static final String PAGE_NUM = "pageNum";
    public static final String PAGE_SIZE = "pageSize";


    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdateBatchByMultiId(java.util.Collection<T> entityList) {
        return saveOrUpdateBatchByMultiId(entityList, com.baomidou.mybatisplus.extension.service.IService.DEFAULT_BATCH_SIZE);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdateBatchByMultiId(java.util.Collection<T> entityList, int batchSize) {
        if (entityList == null || entityList.isEmpty()) {
            return false;
        }
        if (batchSize < 1) {
            batchSize = com.baomidou.mybatisplus.extension.service.IService.DEFAULT_BATCH_SIZE;
        }

        // MP 3.5.x：用 getEntityClass() 取实体类型（替代 currentModelClass()）
        final Class<T> entityClass = this.getEntityClass();

        // 1) 扫描所有 @MppMultiId 字段（支持父类）
        final java.util.List<java.lang.reflect.Field> multiIdFields = new java.util.ArrayList<>();
        final java.util.List<String> multiIdColumns = new java.util.ArrayList<>();

        for (Class<?> c = entityClass; c != null && c != Object.class; c = c.getSuperclass()) {
            for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                com.github.jeffreyning.mybatisplus.anno.MppMultiId ann = f.getAnnotation(com.github.jeffreyning.mybatisplus.anno.MppMultiId.class);
                if (ann == null) {
                    continue;
                }
                f.setAccessible(true);
                multiIdFields.add(f);

                // 注解value优先；为空则用字段名转下划线
                String col = ann.value();
                if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isBlank(col)) {
                    col = com.baomidou.mybatisplus.core.toolkit.StringUtils.camelToUnderline(f.getName());
                }
                multiIdColumns.add(col);
            }
        }

        if (multiIdFields.isEmpty()) {
            throw new IllegalStateException("saveOrUpdateBatchByMultiId：实体 " + entityClass.getName() + " 未声明任何 @MppMultiId 字段，无法按联合主键执行 saveOrUpdate。");
        }

        // 2) 可选：避免更新时把@TableId对应的主键字段也更新掉（如果实体对象里刚好带了id值）
        final com.baomidou.mybatisplus.core.metadata.TableInfo tableInfo = com.baomidou.mybatisplus.core.metadata.TableInfoHelper.getTableInfo(entityClass);
        final String keyProperty = (tableInfo == null ? null : tableInfo.getKeyProperty());
        final java.lang.reflect.Field tableIdField;
        if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isBlank(keyProperty)) {
            tableIdField = null;
        } else {
            java.lang.reflect.Field found = null;
            for (Class<?> c = entityClass; c != null && c != Object.class; c = c.getSuperclass()) {
                try {
                    found = c.getDeclaredField(keyProperty);
                    found.setAccessible(true);
                    break;
                } catch (NoSuchFieldException ignore) {
                }
            }
            tableIdField = found;
        }

        // 3) statementId（MP内部封装的SQL）
        final String insertStatement = getSqlStatement(com.baomidou.mybatisplus.core.enums.SqlMethod.INSERT_ONE);
        final String updateStatement = getSqlStatement(com.baomidou.mybatisplus.core.enums.SqlMethod.UPDATE);
        final String countStatement = getSqlStatement(com.baomidou.mybatisplus.core.enums.SqlMethod.SELECT_COUNT);

        // 4) 批处理：0条->insert；>=1条->update(更新所有命中记录)
        return this.executeBatch(entityList, batchSize, (sqlSession, entity) -> {

            // where: multiId1=? AND multiId2=? ...
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<T> where = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();

            for (int i = 0; i < multiIdFields.size(); i++) {
                java.lang.reflect.Field f = multiIdFields.get(i);
                Object val;
                try {
                    val = f.get(entity);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }

                // 联合主键字段不能为空，否则where不完整会导致误更新/误插入
                if (val == null || (val instanceof CharSequence && com.baomidou.mybatisplus.core.toolkit.StringUtils.isBlank(val.toString()))) {
                    throw new IllegalArgumentException("saveOrUpdateBatchByMultiId：联合主键字段[" + f.getName() + "] 不能为空，entity=" + entity);
                }

                where.eq(multiIdColumns.get(i), val);
            }

            // 先count判断是否存在（不要依赖update返回行数判断存在与否，MySQL值不变会返回0）
            org.apache.ibatis.binding.MapperMethod.ParamMap<Object> countParam = new org.apache.ibatis.binding.MapperMethod.ParamMap<>();
            countParam.put(com.baomidou.mybatisplus.core.toolkit.Constants.WRAPPER, where);

            Number n = sqlSession.selectOne(countStatement, countParam);
            long count = (n == null ? 0L : n.longValue());

            if (count <= 0L) {
                // 不存在 -> 插入
                sqlSession.insert(insertStatement, entity);
                return;
            }

            // 存在(>=1条) -> 更新全部命中记录
            Object oldId = null;
            boolean clearedId = false;
            if (tableIdField != null) {
                try {
                    oldId = tableIdField.get(entity);
                    if (oldId != null) {
                        tableIdField.set(entity, null); // 避免把主键id更新掉
                        clearedId = true;
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }

            try {
                org.apache.ibatis.binding.MapperMethod.ParamMap<Object> updateParam = new org.apache.ibatis.binding.MapperMethod.ParamMap<>();
                updateParam.put(com.baomidou.mybatisplus.core.toolkit.Constants.ENTITY, entity);
                updateParam.put(com.baomidou.mybatisplus.core.toolkit.Constants.WRAPPER, where);

                sqlSession.update(updateStatement, updateParam);
            } finally {
                // 还原id字段，避免影响调用方对象
                if (clearedId) {
                    try {
                        tableIdField.set(entity, oldId);
                    } catch (IllegalAccessException ignore) {
                    }
                }
            }
        });
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public boolean updateByMultiId(T entity) {
        if (entity == null) {
            return false;
        }
        return updateBatchByMultiId(java.util.Collections.singletonList(entity),
                com.baomidou.mybatisplus.extension.service.IService.DEFAULT_BATCH_SIZE);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public boolean updateBatchByMultiId(java.util.Collection<T> entityList) {
        return updateBatchByMultiId(entityList, com.baomidou.mybatisplus.extension.service.IService.DEFAULT_BATCH_SIZE);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public boolean updateBatchByMultiId(java.util.Collection<T> entityList, int batchSize) {
        if (entityList == null || entityList.isEmpty()) {
            return false;
        }
        if (batchSize < 1) {
            batchSize = com.baomidou.mybatisplus.extension.service.IService.DEFAULT_BATCH_SIZE;
        }

        final Class<T> entityClass = this.getEntityClass();

        final java.util.List<java.lang.reflect.Field> multiIdFields = new java.util.ArrayList<>();
        final java.util.List<String> multiIdColumns = new java.util.ArrayList<>();

        for (Class<?> c = entityClass; c != null && c != Object.class; c = c.getSuperclass()) {
            for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                com.github.jeffreyning.mybatisplus.anno.MppMultiId ann = f.getAnnotation(com.github.jeffreyning.mybatisplus.anno.MppMultiId.class);
                if (ann == null) {
                    continue;
                }
                f.setAccessible(true);
                multiIdFields.add(f);

                String col = ann.value();
                if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isBlank(col)) {
                    col = com.baomidou.mybatisplus.core.toolkit.StringUtils.camelToUnderline(f.getName());
                }
                multiIdColumns.add(col);
            }
        }

        if (multiIdFields.isEmpty()) {
            throw new IllegalStateException("updateBatchByMultiId：实体 " + entityClass.getName() + " 未声明任何 @MppMultiId 字段，无法按联合主键执行 update。");
        }

        final com.baomidou.mybatisplus.core.metadata.TableInfo tableInfo = com.baomidou.mybatisplus.core.metadata.TableInfoHelper.getTableInfo(entityClass);
        final String keyProperty = (tableInfo == null ? null : tableInfo.getKeyProperty());
        final java.lang.reflect.Field tableIdField;
        if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isBlank(keyProperty)) {
            tableIdField = null;
        } else {
            java.lang.reflect.Field found = null;
            for (Class<?> c = entityClass; c != null && c != Object.class; c = c.getSuperclass()) {
                try {
                    found = c.getDeclaredField(keyProperty);
                    found.setAccessible(true);
                    break;
                } catch (NoSuchFieldException ignore) {
                }
            }
            tableIdField = found;
        }

        final String updateStatement = getSqlStatement(com.baomidou.mybatisplus.core.enums.SqlMethod.UPDATE);

        return this.executeBatch(entityList, batchSize, (sqlSession, entity) -> {
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<T> where = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();

            for (int i = 0; i < multiIdFields.size(); i++) {
                java.lang.reflect.Field f = multiIdFields.get(i);
                Object val;
                try {
                    val = f.get(entity);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }

                if (val == null || (val instanceof CharSequence && com.baomidou.mybatisplus.core.toolkit.StringUtils.isBlank(val.toString()))) {
                    throw new IllegalArgumentException("updateBatchByMultiId：联合主键字段[" + f.getName() + "] 不能为空，entity=" + entity);
                }

                where.eq(multiIdColumns.get(i), val);
            }

            Object oldId = null;
            boolean clearedId = false;
            if (tableIdField != null) {
                try {
                    oldId = tableIdField.get(entity);
                    if (oldId != null) {
                        tableIdField.set(entity, null);
                        clearedId = true;
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }

            try {
                org.apache.ibatis.binding.MapperMethod.ParamMap<Object> updateParam = new org.apache.ibatis.binding.MapperMethod.ParamMap<>();
                updateParam.put(com.baomidou.mybatisplus.core.toolkit.Constants.ENTITY, entity);
                updateParam.put(com.baomidou.mybatisplus.core.toolkit.Constants.WRAPPER, where);

                sqlSession.update(updateStatement, updateParam);
            } finally {
                if (clearedId) {
                    try {
                        tableIdField.set(entity, oldId);
                    } catch (IllegalAccessException ignore) {
                    }
                }
            }
        });
    }

    @Override
    public T selectByMultiId(T entity) {
        return selectOneByMultiId(entity);
    }

    @Override
    public java.util.List<T> selectListByMultiId(T entity) {
        if (entity == null) {
            return java.util.Collections.emptyList();
        }
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<T> where = buildMultiIdWrapper(entity, "selectListByMultiId");
        return this.baseMapper.selectList(where);
    }

    @Override
    public T selectOneByMultiId(T entity) {
        if (entity == null) {
            return null;
        }
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<T> where = buildMultiIdWrapper(entity, "selectOneByMultiId");
        where.last("limit 1");
        java.util.List<T> list = this.baseMapper.selectList(where);
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    private com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<T> buildMultiIdWrapper(T entity, String opName) {
        final Class<T> entityClass = this.getEntityClass();

        final java.util.List<java.lang.reflect.Field> multiIdFields = new java.util.ArrayList<>();
        final java.util.List<String> multiIdColumns = new java.util.ArrayList<>();

        for (Class<?> c = entityClass; c != null && c != Object.class; c = c.getSuperclass()) {
            for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                com.github.jeffreyning.mybatisplus.anno.MppMultiId ann = f.getAnnotation(com.github.jeffreyning.mybatisplus.anno.MppMultiId.class);
                if (ann == null) {
                    continue;
                }
                f.setAccessible(true);
                multiIdFields.add(f);

                String col = ann.value();
                if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isBlank(col)) {
                    col = com.baomidou.mybatisplus.core.toolkit.StringUtils.camelToUnderline(f.getName());
                }
                multiIdColumns.add(col);
            }
        }

        if (multiIdFields.isEmpty()) {
            throw new IllegalStateException(opName + "：实体 " + entityClass.getName() + " 未声明任何 @MppMultiId 字段，无法按联合主键执行查询。");
        }

        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<T> where = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        for (int i = 0; i < multiIdFields.size(); i++) {
            java.lang.reflect.Field f = multiIdFields.get(i);
            Object val;
            try {
                val = f.get(entity);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            if (val == null || (val instanceof CharSequence && com.baomidou.mybatisplus.core.toolkit.StringUtils.isBlank(val.toString()))) {
                throw new IllegalArgumentException(opName + "：联合主键字段[" + f.getName() + "] 不能为空，entity=" + entity);
            }
            where.eq(multiIdColumns.get(i), val);
        }

        return where;
    }

    public List<T> findPaginatedList(Map<String, String> paramMap) {
        try {
            int pageNum = Integer.parseInt(paramMap.get(PAGE_NUM));
            int pageSize = Integer.parseInt(paramMap.get(PAGE_SIZE));
            return findPaginatedList(paramMap, pageNum, pageSize);
        } catch (Exception e) {
            return findPaginatedList(Collections.emptyMap(), 1, 10);
        }
    }

    /**
     * 根据指定字段的键值对批量查询记录。 相当于 SQL: SELECT * FROM xxx WHERE key1 = val1 AND key2 = val2 ...
     *
     * @param paramMap 字段名-值的映射
     * @return 查询结果列表
     */
    public List<T> findPaginatedList(Map<String, String> paramMap, int pageNum, int pageSize) {
        try (Page<Object> ignored = PageHelper.startPage(pageNum, pageSize)) {
            List<T> list;
            if (paramMap == null || paramMap.isEmpty()) {
                list = this.baseMapper.selectList(new QueryWrapper<>());
            } else {
                // 转换成下划线 map
                Map<String, Object> dbColumnMap = new HashMap<>();
                for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                    if (StringUtils.isBlank(entry.getKey()) || StringUtils.isBlank(entry.getValue()) || PAGE_NUM.equals(entry.getKey()) || PAGE_SIZE.equals(entry.getKey())) {
                        continue;
                    }
                    String dbKey = StringUtils.camelToUnderline(entry.getKey());
                    dbColumnMap.put(dbKey, entry.getValue());
                }
                QueryWrapper<T> wrapper = new QueryWrapper<>();
                // 默认都用like
                dbColumnMap.forEach(wrapper::like);
                list = this.baseMapper.selectList(wrapper);
            }
            return list;
        }

    }

}
