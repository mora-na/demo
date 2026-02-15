package com.example.demo.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页结果载体，包含总数、数据列表与分页信息。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult<T> {
    private long total;
    private List<T> data;
    private int pageNum;
    private int pageSize;
}
