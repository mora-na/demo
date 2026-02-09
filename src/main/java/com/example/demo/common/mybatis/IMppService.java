package com.example.demo.common.mybatis;

/**
 * 多字段联合操作服务接口，基于 @MppMultiField 执行增删改查。
 *
 * @param <T> 实体类型
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
public interface IMppService<T> extends com.github.jeffreyning.mybatisplus.service.IMppService<T> {
    /**
     * 批量保存或更新，按联合字段判断存在性。
     *
     * @param entityList 实体集合
     * @return true 表示执行成功
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    boolean saveOrUpdateBatchByMultiField(java.util.Collection<T> entityList);

    /**
     * 批量保存或更新，按联合字段判断存在性，支持批次大小。
     *
     * @param entityList 实体集合
     * @param batchSize  批次大小
     * @return true 表示执行成功
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    boolean saveOrUpdateBatchByMultiField(java.util.Collection<T> entityList, int batchSize);

    /**
     * 按联合字段更新单条记录。
     *
     * @param entity 实体
     * @return true 表示执行成功
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    boolean updateByMultiField(T entity);

    /**
     * 按联合字段批量更新。
     *
     * @param entityList 实体集合
     * @return true 表示执行成功
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    boolean updateBatchByMultiField(java.util.Collection<T> entityList);

    /**
     * 按联合字段批量更新，支持批次大小。
     *
     * @param entityList 实体集合
     * @param batchSize  批次大小
     * @return true 表示执行成功
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    boolean updateBatchByMultiField(java.util.Collection<T> entityList, int batchSize);

    /**
     * 按联合字段查询列表。
     *
     * @param entity 查询实体
     * @return 匹配的实体列表
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    java.util.List<T> selectByMultiField(T entity);

    /**
     * 按联合字段查询单条记录。
     *
     * @param entity 查询实体
     * @return 单条实体，未命中返回 null
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    T selectOneByMultiField(T entity);
}
