package com.example.demo.common.mybatis;

public class MppServiceImpl<M extends com.example.demo.common.mybatis.MppBaseMapper<T>, T> extends com.github.jeffreyning.mybatisplus.service.MppServiceImpl<M, T> implements com.example.demo.common.mybatis.IMppService<T> {

    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    @Override
    public boolean saveOrUpdateBatchByMultiField(java.util.Collection<T> entityList) {
        return saveOrUpdateBatchByMultiField(entityList, com.baomidou.mybatisplus.extension.service.IService.DEFAULT_BATCH_SIZE);
    }

    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    @Override
    public boolean saveOrUpdateBatchByMultiField(java.util.Collection<T> entityList, int batchSize) {
        if (entityList == null || entityList.isEmpty()) {
            return false;
        }
        if (batchSize < 1) {
            batchSize = com.baomidou.mybatisplus.extension.service.IService.DEFAULT_BATCH_SIZE;
        }

        // MP 3.5.x：用 getEntityClass() 取实体类型（替代 currentModelClass()）
        final Class<T> entityClass = this.getEntityClass();

        final MultiFieldMeta multiFieldMeta = resolveMultiFieldMeta(entityClass);
        if (multiFieldMeta.fields.isEmpty()) {
            throw new IllegalStateException("saveOrUpdateBatchByMultiField：实体 " + entityClass.getName() + " 未声明任何 @MppMultiField 字段，无法按联合字段执行 saveOrUpdate。");
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

            // where: multiField1=? AND multiField2=? ...
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<T> where = buildMultiFieldWrapper(entity, "saveOrUpdateBatchByMultiField", multiFieldMeta);

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

    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateByMultiField(T entity) {
        if (entity == null) {
            return false;
        }
        return updateBatchByMultiField(java.util.Collections.singletonList(entity), com.baomidou.mybatisplus.extension.service.IService.DEFAULT_BATCH_SIZE);
    }

    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateBatchByMultiField(java.util.Collection<T> entityList) {
        return updateBatchByMultiField(entityList, com.baomidou.mybatisplus.extension.service.IService.DEFAULT_BATCH_SIZE);
    }

    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateBatchByMultiField(java.util.Collection<T> entityList, int batchSize) {
        if (entityList == null || entityList.isEmpty()) {
            return false;
        }
        if (batchSize < 1) {
            batchSize = com.baomidou.mybatisplus.extension.service.IService.DEFAULT_BATCH_SIZE;
        }

        final Class<T> entityClass = this.getEntityClass();

        final MultiFieldMeta multiFieldMeta = resolveMultiFieldMeta(entityClass);
        if (multiFieldMeta.fields.isEmpty()) {
            throw new IllegalStateException("updateBatchByMultiField：实体 " + entityClass.getName() + " 未声明任何 @MppMultiField 字段，无法按联合字段执行 update。");
        }

        final com.baomidou.mybatisplus.core.metadata.TableInfo tableInfo = com.baomidou.mybatisplus.core.metadata.TableInfoHelper.getTableInfo(entityClass);
        final String keyProperty = (tableInfo == null ? null : tableInfo.getKeyProperty());
        final java.lang.reflect.Field tableIdField = resolveTableIdField(entityClass, keyProperty);

        final String updateStatement = getSqlStatement(com.baomidou.mybatisplus.core.enums.SqlMethod.UPDATE);

        return this.executeBatch(entityList, batchSize, (sqlSession, entity) -> {
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<T> where = buildMultiFieldWrapper(entity, "updateBatchByMultiField", multiFieldMeta);

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
    public java.util.List<T> selectByMultiField(T entity) {
        if (entity == null) {
            return java.util.Collections.emptyList();
        }
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<T> where = buildMultiFieldWrapper(entity, "selectListByMultiField");
        return this.baseMapper.selectList(where);
    }

    @Override
    public T selectOneByMultiField(T entity) {
        if (entity == null) {
            return null;
        }
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<T> where = buildMultiFieldWrapper(entity, "selectOneByMultiField");
        where.last("limit 1");
        java.util.List<T> list = this.baseMapper.selectList(where);
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    private com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<T> buildMultiFieldWrapper(T entity, String opName) {
        final Class<T> entityClass = this.getEntityClass();

        final MultiFieldMeta multiFieldMeta = resolveMultiFieldMeta(entityClass);
        if (multiFieldMeta.fields.isEmpty()) {
            throw new IllegalStateException(opName + "：实体 " + entityClass.getName() + " 未声明任何 @MppMultiField 字段，无法按联合字段执行查询。");
        }

        return buildMultiFieldWrapper(entity, opName, multiFieldMeta);
    }

    private com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<T> buildMultiFieldWrapper(T entity, String opName, MultiFieldMeta multiFieldMeta) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<T> where = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        for (int i = 0; i < multiFieldMeta.fields.size(); i++) {
            java.lang.reflect.Field f = multiFieldMeta.fields.get(i);
            Object val;
            try {
                val = f.get(entity);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

//            if (val == null || (val instanceof CharSequence && com.baomidou.mybatisplus.core.toolkit.StringUtils.isBlank(val.toString()))) {
//                throw new IllegalArgumentException(opName + "：联合字段[" + f.getName() + "] 不能为空，entity=" + entity);
//            }
            String column = multiFieldMeta.columns.get(i);
            if (val == null) {
                where.isNull(column);
            } else {
                where.eq(column, val);
            }
        }

        return where;
    }

    private MultiFieldMeta resolveMultiFieldMeta(Class<T> entityClass) {
        final java.util.List<java.lang.reflect.Field> multiFieldFields = new java.util.ArrayList<>();
        final java.util.List<String> multiFieldColumns = new java.util.ArrayList<>();

        final com.baomidou.mybatisplus.core.metadata.TableInfo tableInfo = com.baomidou.mybatisplus.core.metadata.TableInfoHelper.getTableInfo(entityClass);
        final java.util.Map<String, String> propertyColumnMap = new java.util.HashMap<>();
        if (tableInfo != null) {
            if (!com.baomidou.mybatisplus.core.toolkit.StringUtils.isBlank(tableInfo.getKeyProperty()) && !com.baomidou.mybatisplus.core.toolkit.StringUtils.isBlank(tableInfo.getKeyColumn())) {
                propertyColumnMap.put(tableInfo.getKeyProperty(), tableInfo.getKeyColumn());
            }
            if (tableInfo.getFieldList() != null) {
                for (com.baomidou.mybatisplus.core.metadata.TableFieldInfo fieldInfo : tableInfo.getFieldList()) {
                    propertyColumnMap.put(fieldInfo.getProperty(), fieldInfo.getColumn());
                }
            }
        }

        for (Class<?> c = entityClass; c != null && c != Object.class; c = c.getSuperclass()) {
            for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
                    continue;
                }
                com.example.demo.common.annotation.MppMultiField ann = f.getAnnotation(com.example.demo.common.annotation.MppMultiField.class);
                if (ann == null) {
                    continue;
                }
                f.setAccessible(true);
                multiFieldFields.add(f);

                String col = ann.value();
                if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isBlank(col)) {
                    col = propertyColumnMap.get(f.getName());
                }
                if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isBlank(col)) {
                    col = com.baomidou.mybatisplus.core.toolkit.StringUtils.camelToUnderline(f.getName());
                }
                multiFieldColumns.add(col);
            }
        }

        return new MultiFieldMeta(multiFieldFields, multiFieldColumns);
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

    private static class MultiFieldMeta {
        private final java.util.List<java.lang.reflect.Field> fields;
        private final java.util.List<String> columns;

        private MultiFieldMeta(java.util.List<java.lang.reflect.Field> fields, java.util.List<String> columns) {
            this.fields = fields;
            this.columns = columns;
        }
    }

}
