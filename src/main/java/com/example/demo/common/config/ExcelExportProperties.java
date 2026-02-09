package com.example.demo.common.config;

import com.example.demo.common.tool.ExcelTool;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Data
@Component
@ConfigurationProperties(prefix = "excel.export")
public class ExcelExportProperties {

    private int pageSize = ExcelTool.getDefaultExportPageSize();

    @PostConstruct
    public void apply() {
        ExcelTool.setDefaultExportPageSize(pageSize);
    }
}
