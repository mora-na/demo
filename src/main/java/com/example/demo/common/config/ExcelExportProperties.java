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
    private int rowWindowSize = ExcelTool.getDefaultRowWindowSize();
    private boolean useSharedStrings = ExcelTool.isDefaultUseSharedStringsTable();
    private boolean autoSize = ExcelTool.isDefaultAutoSize();
    private int autoSizeMaxRows = ExcelTool.getDefaultAutoSizeMaxRows();
    private boolean countEnabled = ExcelTool.isDefaultCountEnabled();
    private boolean compressTempFiles = ExcelTool.isDefaultCompressTempFiles();

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
