package com.example.demo.common.model;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import java.io.Serializable;

/**
 * 分页参数与排序信息。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码
     */
    @Min(1)
    private Integer pageNum = 1;

    /**
     * 每页条数
     */
    @Range(min = 1, max = 500)
    private Integer pageSize = 10;

    /**
     * 排序字段
     */
    private String orderByColumn;

    /**
     * 排序方向 asc/desc
     */
    private String isAsc;

    /**
     * 构建 MyBatis-Plus 的 Page 对象。
     */
    public <T> Page<T> buildPage() {
        int current = pageNum == null ? 1 : Math.max(pageNum, 1);
        int size = pageSize == null ? 10 : Math.max(pageSize, 1);
        size = Math.min(size, 500);
        Page<T> page = new Page<>(current, size);
        String orderColumn = StringUtils.trimToNull(orderByColumn);
        if (orderColumn != null) {
            boolean asc = "asc".equalsIgnoreCase(isAsc);
            page.addOrder(asc ? OrderItem.asc(orderColumn) : OrderItem.desc(orderColumn));
        }
        return page;
    }
}
