package com.example.demo.framework.service;

public interface IMppService<T> extends com.github.jeffreyning.mybatisplus.service.IMppService<T> {
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    boolean saveOrUpdateBatchByMultiField(java.util.Collection<T> entityList);

    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    boolean saveOrUpdateBatchByMultiField(java.util.Collection<T> entityList, int batchSize);

    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    boolean updateByMultiField(T entity);

    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    boolean updateBatchByMultiField(java.util.Collection<T> entityList);

    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    boolean updateBatchByMultiField(java.util.Collection<T> entityList, int batchSize);

    java.util.List<T> selectByMultiField(T entity);

    T selectOneByMultiField(T entity);
}
