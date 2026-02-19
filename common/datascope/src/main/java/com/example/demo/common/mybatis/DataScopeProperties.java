package com.example.demo.common.mybatis;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 数据范围配置项，绑定 security.data-scope 前缀。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Data
@Component
@Validated
@ConfigurationProperties(prefix = "security.data-scope")
public class DataScopeProperties {

    private boolean enabled = true;

    private String source = "db";

    private String defaultType = DataScopeType.SELF;

    @Min(0)
    private long cacheSeconds = 0;

    private Map<String, String> tableColumnMap = new LinkedHashMap<>();

    /**
     * SQL 解析失败时是否放行（true 表示放行，false 表示阻断）。
     */
    private boolean failOpenOnSqlParseError = true;

}
