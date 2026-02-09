package com.example.demo.common.tool;

import com.example.demo.common.annotation.ExcelColumn;
import com.example.demo.common.exception.ExcelProcessException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Excel 导入导出工具。
 */
public final class ExcelTool {

    public static final String XLSX_SUFFIX = ".xlsx";
    private static final String DEFAULT_SHEET = "Sheet1";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DataFormatter DATA_FORMATTER = new DataFormatter();
    private static final Cache<Class<?>, List<FieldMeta>> FIELD_CACHE = Caffeine.newBuilder()
            .maximumSize(256)
            .weakKeys()
            .build();

    private ExcelTool() {
    }

    /**
     * 导出到内存流，供 Web 响应直接使用。
     */
    public static <T> ByteArrayOutputStream exportToStream(List<T> data, Class<T> type) {
        return exportToStream(data, type, DEFAULT_SHEET);
    }

    public static <T> ByteArrayOutputStream exportToStream(List<T> data, Class<T> type, String sheetName) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exportToStream(data, type, sheetName, outputStream);
        return outputStream;
    }

    public static <T> void exportToStream(List<T> data, Class<T> type, String sheetName, OutputStream outputStream) {
        Objects.requireNonNull(type, "导出的实体类型不能为空");
        Objects.requireNonNull(outputStream, "输出流不能为空");
        if (data == null) {
            data = new ArrayList<>();
        }
        List<FieldMeta> fieldMetas = getFieldMetas(type);
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(200)) {
            workbook.setCompressTempFiles(true);
            CreationHelper creationHelper = workbook.getCreationHelper();
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(creationHelper.createDataFormat().getFormat("yyyy-mm-dd hh:mm:ss"));

            SXSSFSheet sheet = workbook.createSheet(StringUtils.defaultIfBlank(sheetName, DEFAULT_SHEET));
            if (sheet != null) {
                sheet.trackAllColumnsForAutoSizing();
            }
            if (sheet != null) {
                buildHeaderRow(sheet, fieldMetas);
            }

            int rowIndex = 1;
            for (T item : data) {
                Row row = null;
                if (sheet != null) {
                    row = sheet.createRow(rowIndex++);
                }
                writeDataRow(row, fieldMetas, item, dateStyle);
            }

            autoSizeColumns(sheet, fieldMetas.size(), data.size());

            workbook.write(outputStream);
            outputStream.flush();
        } catch (IOException e) {
            throw new ExcelProcessException("导出 Excel 失败", e);
        }
    }

    /**
     * 导出到指定目录文件。
     */
    public static <T> File exportToFile(List<T> data, Class<T> type, Path directory, String fileName) {
        Objects.requireNonNull(directory, "导出目录不能为空");
        Objects.requireNonNull(fileName, "文件名不能为空");
        String targetName = ensureXlsxSuffix(fileName);
        try {
            Files.createDirectories(directory);
            Path target = directory.resolve(targetName);
            try (ByteArrayOutputStream outputStream = exportToStream(data, type)) {
                Files.write(target, outputStream.toByteArray());
            }
            return target.toFile();
        } catch (IOException e) {
            throw new ExcelProcessException("写入 Excel 文件失败", e);
        }
    }

    public static <T> List<T> importFromFile(File file, Class<T> type) {
        return importFromFile(file, type, 0);
    }

    public static <T> List<T> importFromFile(File file, Class<T> type, int headerRowIndex) {
        Objects.requireNonNull(file, "导入文件不能为空");
        if (!file.exists()) {
            throw new ExcelProcessException("导入文件不存在: " + file.getAbsolutePath());
        }
        try (InputStream inputStream = Files.newInputStream(file.toPath())) {
            return importFromStream(inputStream, type, headerRowIndex);
        } catch (IOException e) {
            throw new ExcelProcessException("读取导入文件失败", e);
        }
    }

    public static <T> List<T> importFromMultipart(MultipartFile multipartFile, Class<T> type) {
        return importFromMultipart(multipartFile, type, 0);
    }

    public static <T> List<T> importFromMultipart(MultipartFile multipartFile, Class<T> type, int headerRowIndex) {
        Objects.requireNonNull(multipartFile, "上传文件不能为空");
        if (multipartFile.isEmpty()) {
            throw new ExcelProcessException("上传的 Excel 文件为空");
        }
        try (InputStream inputStream = multipartFile.getInputStream()) {
            return importFromStream(inputStream, type, headerRowIndex);
        } catch (IOException e) {
            throw new ExcelProcessException("读取上传的 Excel 文件失败", e);
        }
    }

    public static <T> List<T> importFromStream(InputStream inputStream, Class<T> type, int headerRowIndex) {
        Objects.requireNonNull(inputStream, "输入流不能为空");
        Objects.requireNonNull(type, "导入的实体类型不能为空");
        if (headerRowIndex < 0) {
            throw new ExcelProcessException("表头行下标不能小于 0");
        }
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
            if (sheet == null) {
                return new ArrayList<>();
            }
            List<FieldMeta> fieldMetas = getFieldMetas(type);
            return readSheet(sheet, fieldMetas, type, headerRowIndex);
        } catch (IOException e) {
            throw new ExcelProcessException("解析 Excel 文件失败", e);
        }
    }

    private static void buildHeaderRow(Sheet sheet, List<FieldMeta> fieldMetas) {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < fieldMetas.size(); i++) {
            Cell cell = headerRow.createCell(i, CellType.STRING);
            cell.setCellValue(fieldMetas.get(i).getHeaderName());
        }
    }

    private static <T> void writeDataRow(Row row, List<FieldMeta> fieldMetas, T item, CellStyle dateStyle) {
        if (item == null) {
            return;
        }
        for (int i = 0; i < fieldMetas.size(); i++) {
            FieldMeta meta = fieldMetas.get(i);
            Cell cell = row.createCell(i);
            Object value = meta.getValue(item);
            setCellValue(cell, value, dateStyle);
        }
    }

    private static void setCellValue(Cell cell, Object value, CellStyle dateStyle) {
        if (value == null) {
            return;
        }
        if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof LocalDateTime) {
            LocalDateTime localDateTime = (LocalDateTime) value;
            Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
            cell.setCellValue(date);
            cell.setCellStyle(dateStyle);
        } else if (value instanceof LocalDate) {
            LocalDate localDate = (LocalDate) value;
            Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            cell.setCellValue(date);
            cell.setCellStyle(dateStyle);
        } else if (value instanceof Date) {
            cell.setCellValue((Date) value);
            cell.setCellStyle(dateStyle);
        } else {
            cell.setCellValue(String.valueOf(value));
        }
    }

    private static List<FieldMeta> getFieldMetas(Class<?> type) {
        List<FieldMeta> metas = FIELD_CACHE.get(type, ExcelTool::resolveFieldMetas);
        if (metas == null) {
            throw new ExcelProcessException("无法解析 Excel 字段元数据");
        }
        return metas;
    }

    private static List<FieldMeta> resolveFieldMetas(Class<?> type) {
        List<FieldMeta> metas = new ArrayList<>();
        for (java.lang.reflect.Field field : collectFields(type)) {
            ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);
            if (annotation != null && !annotation.exit()) {
                continue;
            }
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if ("serialVersionUID".equals(field.getName())) {
                continue;
            }
            String headerName = annotation != null && StringUtils.isNotBlank(annotation.headerName()) ? annotation.headerName().trim() : field.getName();
            Map<String, String> exportMapping = parseMapping(annotation);
            FieldMeta meta = new FieldMeta(field, headerName, exportMapping);
            if (metas.stream().anyMatch(m -> m.getHeaderName().equalsIgnoreCase(headerName))) {
                throw new ExcelProcessException("表头重复: " + headerName);
            }
            metas.add(meta);
        }
        if (metas.isEmpty()) {
            throw new ExcelProcessException("实体类没有可导入导出的字段");
        }
        return metas;
    }

    private static List<java.lang.reflect.Field> collectFields(Class<?> type) {
        List<java.lang.reflect.Field> fields = new ArrayList<>();
        if (type == null || Object.class.equals(type)) {
            return fields;
        }
        fields.addAll(collectFields(type.getSuperclass()));
        fields.addAll(Arrays.asList(type.getDeclaredFields()));
        return fields;
    }

    private static <T> List<T> readSheet(Sheet sheet, List<FieldMeta> fieldMetas, Class<T> type, int headerRowIndex) {
        Row headerRow = sheet.getRow(headerRowIndex);
        if (headerRow == null) {
            throw new ExcelProcessException("未找到表头行，索引: " + headerRowIndex);
        }

        Map<Integer, FieldMeta> columnMapping = mapColumns(headerRow, fieldMetas);
        List<T> result = new ArrayList<>();
        int firstDataRow = headerRowIndex + 1;
        for (int rowIndex = firstDataRow; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }
            T instance = newInstance(type);
            boolean hasValue = false;
            for (Map.Entry<Integer, FieldMeta> entry : columnMapping.entrySet()) {
                Cell cell = row.getCell(entry.getKey());
                FieldMeta meta = entry.getValue();
                Object rawValue = meta.applyImportMapping(readCellValueForType(cell, meta.getField().getType()));
                Object converted;
                try {
                    converted = convertValue(rawValue, meta.getField().getType());
                } catch (ExcelProcessException e) {
                    Object fallbackRaw = meta.applyImportMapping(readCellValueFallback(cell, meta.getField().getType()));
                    converted = convertValue(fallbackRaw, meta.getField().getType());
                }
                if (converted != null) {
                    meta.setValue(instance, converted);
                    hasValue = true;
                }
            }
            if (hasValue) {
                result.add(instance);
            }
        }
        return result;
    }

    private static Map<Integer, FieldMeta> mapColumns(Row headerRow, List<FieldMeta> fieldMetas) {
        Map<String, FieldMeta> headerMap = fieldMetas.stream().collect(Collectors.toMap(meta -> meta.getHeaderName().toLowerCase(Locale.ROOT), meta -> meta, (a, b) -> a, LinkedHashMap::new));
        Map<String, FieldMeta> fieldNameMap = fieldMetas.stream().collect(Collectors.toMap(meta -> meta.getField().getName().toLowerCase(Locale.ROOT), meta -> meta, (a, b) -> a, LinkedHashMap::new));
        Map<Integer, FieldMeta> columnMapping = new HashMap<>();
        Set<FieldMeta> mappedFields = new HashSet<>();
        short lastCellNum = headerRow.getLastCellNum();
        for (int i = 0; i < lastCellNum; i++) {
            buildColumnMappingFromHeaderRow(headerRow, headerMap, columnMapping, mappedFields, i);
        }
        for (int i = 0; i < lastCellNum; i++) {
            if (columnMapping.containsKey(i)) {
                continue;
            }
            buildColumnMappingFromHeaderRow(headerRow, fieldNameMap, columnMapping, mappedFields, i);
        }
        if (columnMapping.isEmpty()) {
            throw new ExcelProcessException("表头无法与实体字段匹配");
        }
        return columnMapping;
    }

    private static void buildColumnMappingFromHeaderRow(Row headerRow, Map<String, FieldMeta> headerMap, Map<Integer, FieldMeta> columnMapping, Set<FieldMeta> mappedFields, int i) {
        Cell cell = headerRow.getCell(i);
        String headerName = toHeaderKey(cell);
        if (StringUtils.isBlank(headerName)) {
            return;
        }
        FieldMeta meta = headerMap.get(headerName);
        if (meta != null && mappedFields.add(meta)) {
            columnMapping.put(i, meta);
        }
    }

    private static String toHeaderKey(Cell cell) {
        Object value = readCellValue(cell);
        if (value == null) {
            return null;
        }
        return value.toString().trim().toLowerCase(Locale.ROOT);
    }

    private static Object readCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        CellType cellType = cell.getCellType();
        switch (cellType) {
            case STRING:
                return cell.getStringCellValue();
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                }
                return cell.getNumericCellValue();
            case FORMULA:
                return readFormulaValue(cell);
            case BLANK:
            case _NONE:
            case ERROR:
            default:
                return null;
        }
    }

    private static Object readCellValueForType(Cell cell, Class<?> targetType) {
        if (cell == null) {
            return null;
        }
        if (targetType == String.class) {
            return formatCellValue(cell);
        }
        return readCellValue(cell);
    }

    private static Object readCellValueFallback(Cell cell, Class<?> targetType) {
        if (cell == null) {
            return null;
        }
        if (targetType == String.class) {
            return readCellValue(cell);
        }
        return formatCellValue(cell);
    }

    private static String formatCellValue(Cell cell) {
        String formatted = DATA_FORMATTER.formatCellValue(cell);
        if (StringUtils.isBlank(formatted)) {
            return null;
        }
        return formatted;
    }

    private static Object readFormulaValue(Cell cell) {
        CellType cached = cell.getCachedFormulaResultType();
        switch (cached) {
            case STRING:
                return cell.getRichStringCellValue().getString();
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                }
                return cell.getNumericCellValue();
            default:
                return null;
        }
    }

    private static Object convertValue(Object rawValue, Class<?> targetType) {
        if (rawValue == null) {
            return null;
        }
        if (targetType.isAssignableFrom(rawValue.getClass())) {
            return rawValue;
        }
        String rawString = rawValue.toString();
        if (StringUtils.isBlank(rawString)) {
            return null;
        }
        try {
            if (targetType == String.class) {
                return rawString;
            }
            if (targetType == Integer.class || targetType == int.class) {
                return new BigDecimal(rawString).intValue();
            }
            if (targetType == Long.class || targetType == long.class) {
                return new BigDecimal(rawString).longValue();
            }
            if (targetType == Double.class || targetType == double.class) {
                return new BigDecimal(rawString).doubleValue();
            }
            if (targetType == Float.class || targetType == float.class) {
                return new BigDecimal(rawString).floatValue();
            }
            if (targetType == BigDecimal.class) {
                return new BigDecimal(rawString);
            }
            if (targetType == Boolean.class || targetType == boolean.class) {
                return Boolean.parseBoolean(rawString);
            }
            if (targetType == LocalDateTime.class) {
                if (rawValue instanceof Date) {
                    return LocalDateTime.ofInstant(((Date) rawValue).toInstant(), ZoneId.systemDefault());
                }
                try {
                    return LocalDateTime.parse(rawString, DATE_TIME_FORMATTER);
                } catch (Exception ignore) {
                    return LocalDate.parse(rawString, DATE_FORMATTER).atStartOfDay();
                }
            }
            if (targetType == LocalDate.class) {
                if (rawValue instanceof Date) {
                    return ((Date) rawValue).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                }
                return LocalDate.parse(rawString, DATE_FORMATTER);
            }
            if (targetType == Date.class) {
                if (rawValue instanceof Date) {
                    return rawValue;
                }
                if (rawValue instanceof Number) {
                    long time = ((Number) rawValue).longValue();
                    return Date.from(Instant.ofEpochMilli(time));
                }
                if (rawValue instanceof LocalDateTime) {
                    return Date.from(((LocalDateTime) rawValue).atZone(ZoneId.systemDefault()).toInstant());
                }
                if (rawValue instanceof LocalDate) {
                    return Date.from(((LocalDate) rawValue).atStartOfDay(ZoneId.systemDefault()).toInstant());
                }
                try {
                    LocalDateTime dateTime = LocalDateTime.parse(rawString, DATE_TIME_FORMATTER);
                    return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
                } catch (Exception ignore) {
                    LocalDate localDate = LocalDate.parse(rawString, DATE_FORMATTER);
                    return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                }
            }
        } catch (Exception e) {
            throw new ExcelProcessException("单元格值转换失败，值: " + rawString + "，目标类型: " + targetType.getSimpleName(), e);
        }
        return rawValue;
    }

    private static <T> T newInstance(Class<T> type) {
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new ExcelProcessException("创建实体实例失败，请确保存在无参构造函数", e);
        }
    }

    private static void autoSizeColumns(Sheet sheet, int columnCount, int rowCount) {
        if (columnCount <= 0) {
            return;
        }
        if (rowCount > 2000) {
            return;
        }
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
            int currentWidth = sheet.getColumnWidth(i);
            sheet.setColumnWidth(i, Math.min(currentWidth + 512, 255 * 256));
        }
    }

    public static String ensureXlsxSuffix(String fileName) {
        String trimmed = fileName.trim();
        // 忽略大小写判断结尾是否是 .xlsx
        if (trimmed.toLowerCase(Locale.ROOT).endsWith(XLSX_SUFFIX)) {
            return trimmed;
        }
        return trimmed + XLSX_SUFFIX;
    }

    private static Map<String, String> parseMapping(ExcelColumn annotation) {
        Map<String, String> mapping = new LinkedHashMap<>();
        if (annotation == null || annotation.mapping() == null) {
            return mapping;
        }
        for (String rule : annotation.mapping()) {
            if (StringUtils.isBlank(rule)) {
                continue;
            }
            String[] pair = rule.split(":", 2);
            if (pair.length != 2) {
                continue;
            }
            String source = pair[0].trim();
            String target = pair[1].trim();
            if (source.isEmpty()) {
                continue;
            }
            mapping.put(source, target);
        }
        return mapping;
    }

    /**
     * 字段元数据与访问工具。
     */
    private static class FieldMeta {
        @Getter
        private final java.lang.reflect.Field field;
        @Getter
        private final String headerName;
        private final Map<String, String> exportMapping;
        private final Map<String, String> importMapping;

        FieldMeta(java.lang.reflect.Field field, String headerName, Map<String, String> exportMapping) {
            this.field = field;
            this.headerName = headerName;
            this.exportMapping = exportMapping;
            this.importMapping = exportMapping.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey, (a, b) -> a, LinkedHashMap::new));
            this.field.setAccessible(true);
        }

        Object getValue(Object target) {
            try {
                Object original = field.get(target);
                return applyExportMapping(original);
            } catch (IllegalAccessException e) {
                throw new ExcelProcessException("读取字段失败: " + field.getName(), e);
            }
        }

        void setValue(Object target, Object value) {
            try {
                field.set(target, value);
            } catch (IllegalAccessException e) {
                throw new ExcelProcessException("写入字段失败: " + field.getName(), e);
            }
        }

        Object applyExportMapping(Object original) {
            if (original == null || exportMapping.isEmpty()) {
                return original;
            }
            String mapped = resolveMapping(exportMapping, original);
            return mapped != null ? mapped : original;
        }

        Object applyImportMapping(Object raw) {
            if (raw == null || importMapping.isEmpty()) {
                return raw;
            }
            String mapped = resolveMapping(importMapping, raw);
            return mapped != null ? mapped : raw;
        }

        private String resolveMapping(Map<String, String> mapping, Object raw) {
            String key = raw.toString();
            String mapped = mapping.get(key);
            if (mapped != null) {
                return mapped;
            }
            if (raw instanceof Number) {
                return mapping.get(normalizeNumberKey(raw));
            }
            return null;
        }

        private String normalizeNumberKey(Object raw) {
            try {
                return new BigDecimal(raw.toString()).stripTrailingZeros().toPlainString();
            } catch (NumberFormatException ignore) {
                return raw.toString();
            }
        }
    }
}
