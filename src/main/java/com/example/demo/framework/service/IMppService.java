package com.example.demo.framework.service;

public interface IMppService<T> extends com.github.jeffreyning.mybatisplus.service.IMppService<T> {
    java.util.List<T> selectListByMultiId(T entity);

    T selectOneByMultiId(T entity);
}
