package com.example.demo.framework.service.impl;

public class MppServiceImpl<M extends com.example.demo.framework.service.MppBaseMapper<T>, T> extends com.github.jeffreyning.mybatisplus.service.MppServiceImpl<M, T> implements com.example.demo.framework.service.IMppService<T> {

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

        final MultiIdMeta multiIdMeta = resolveMultiIdMeta(entityClass);
        if (multiIdMeta.fields.isEmpty()) {
            throw new IllegalStateException("saveOrUpdateBatchByMultiId：实体 " + entityClass.getName() + " 未声明任何 @MppMultiId 字段，无法按联合主键执行 saveOrUpdate。");
        }

        // 2) 可选：避免更新时把@TableId对应的主键字段也更新掉（如果实体对象里刚好带了id值）
        final com.baomidou.mybatisplus.core.metadata.TableInfo tableInfo = com.baomidou.mybatisplus.core.metadata.TableInfoHelper.getTableInfo(entityClass);
        final String keyProperty = (tableInfo == null ? null : tableInfo.getKeyProperty());
        final java.lang.reflect.Field tableIdField = resolveTableIdField(entityClass, keyProperty);

        // 3) statementId（MP内部封装的SQL）
        final String insertStatement = getSqlStatement(com.baomidou.mybatisplus.core.enums.SqlMethod.INSERT_ONE);
        final String updateStatement = getSqlStatement(com.baomidou.mybatisplus.core.enums.SqlMethod.UPDATE);
        final String countStatement = getSqlStatement(com.baomidou.mybatisplus.core.enums.SqlMethod.SELECT_COUNT);

        // 4) 批处理：0条->insert；>=1条->update(更新所有命中记录)
        return this.executeBatch(entityList, batchSize, (sqlSession, entity) -> {

            // where: multiId1=? AND multiId2=? ...
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<T> where = buildMultiIdWrapper(entity, "saveOrUpdateBatchByMultiId", multiIdMeta);

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
        return updateBatchByMultiId(java.util.Collections.singletonList(entity), com.baomidou.mybatisplus.extension.service.IService.DEFAULT_BATCH_SIZE);
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

        final MultiIdMeta multiIdMeta = resolveMultiIdMeta(entityClass);
        if (multiIdMeta.fields.isEmpty()) {
            throw new IllegalStateException("updateBatchByMultiId：实体 " + entityClass.getName() + " 未声明任何 @MppMultiId 字段，无法按联合主键执行 update。");
        }

        final com.baomidou.mybatisplus.core.metadata.TableInfo tableInfo = com.baomidou.mybatisplus.core.metadata.TableInfoHelper.getTableInfo(entityClass);
        final String keyProperty = (tableInfo == null ? null : tableInfo.getKeyProperty());
        final java.lang.reflect.Field tableIdField = resolveTableIdField(entityClass, keyProperty);

        final String updateStatement = getSqlStatement(com.baomidou.mybatisplus.core.enums.SqlMethod.UPDATE);

        return this.executeBatch(entityList, batchSize, (sqlSession, entity) -> {
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<T> where = buildMultiIdWrapper(entity, "updateBatchByMultiId", multiIdMeta);

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

        final MultiIdMeta multiIdMeta = resolveMultiIdMeta(entityClass);
        if (multiIdMeta.fields.isEmpty()) {
            throw new IllegalStateException(opName + "：实体 " + entityClass.getName() + " 未声明任何 @MppMultiId 字段，无法按联合主键执行查询。");
        }

        return buildMultiIdWrapper(entity, opName, multiIdMeta);
    }

    private com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<T> buildMultiIdWrapper(T entity, String opName, MultiIdMeta multiIdMeta) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<T> where = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        for (int i = 0; i < multiIdMeta.fields.size(); i++) {
            java.lang.reflect.Field f = multiIdMeta.fields.get(i);
            Object val;
            try {
                val = f.get(entity);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            if (val == null || (val instanceof CharSequence && com.baomidou.mybatisplus.core.toolkit.StringUtils.isBlank(val.toString()))) {
                throw new IllegalArgumentException(opName + "：联合主键字段[" + f.getName() + "] 不能为空，entity=" + entity);
            }
            where.eq(multiIdMeta.columns.get(i), val);
        }

        return where;
    }

    private MultiIdMeta resolveMultiIdMeta(Class<T> entityClass) {
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

        return new MultiIdMeta(multiIdFields, multiIdColumns);
    }

    private java.lang.reflect.Field resolveTableIdField(Class<T> entityClass, String keyProperty) {
        if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isBlank(keyProperty)) {
            return null;
        }
        for (Class<?> c = entityClass; c != null && c != Object.class; c = c.getSuperclass()) {
            try {
                java.lang.reflect.Field found = c.getDeclaredField(keyProperty);
                found.setAccessible(true);
                return found;
            } catch (NoSuchFieldException ignore) {
            }
        }
        return null;
    }

    private static class MultiIdMeta {
        private final java.util.List<java.lang.reflect.Field> fields;
        private final java.util.List<String> columns;

        private MultiIdMeta(java.util.List<java.lang.reflect.Field> fields, java.util.List<String> columns) {
            this.fields = fields;
            this.columns = columns;
        }
    }

}
