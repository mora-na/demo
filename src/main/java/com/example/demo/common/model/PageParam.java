package com.example.demo.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分页参数载体，包含页码与每页大小。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageParam {

    private Integer pageNum;
    private Integer pageSize;

}
