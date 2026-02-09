package com.example.demo.common.config;

import com.example.demo.common.tool.ExcelTool;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Excel 导出配置绑定与应用，将配置值同步到 ExcelTool。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Data
@Component
@ConfigurationProperties(prefix = "excel.export")
public class ExcelExportProperties {

    private int pageSize = ExcelTool.getDefaultExportPageSize();
    private int rowWindowSize = ExcelTool.getDefaultRowWindowSize();
    private boolean useSharedStrings = ExcelTool.isDefaultUseSharedStringsTable();
    private boolean autoSize = ExcelTool.isDefaultAutoSize();
    private int autoSizeMaxRows = ExcelTool.getDefaultAutoSizeMaxRows();
    private boolean countEnabled = ExcelTool.isDefaultCountEnabled();
    private boolean compressTempFiles = ExcelTool.isDefaultCompressTempFiles();

    /**
     * 容器初始化后应用配置到导出工具。
     *
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @PostConstruct
    public void apply() {
        ExcelTool.setDefaultExportPageSize(pageSize);
        ExcelTool.setDefaultRowWindowSize(rowWindowSize);
        ExcelTool.setDefaultUseSharedStringsTable(useSharedStrings);
        ExcelTool.setDefaultAutoSize(autoSize);
        ExcelTool.setDefaultAutoSizeMaxRows(autoSizeMaxRows);
        ExcelTool.setDefaultCountEnabled(countEnabled);
        ExcelTool.setDefaultCompressTempFiles(compressTempFiles);
    }
}
