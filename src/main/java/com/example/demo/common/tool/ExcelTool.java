package com.example.demo.common.tool;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.annotation.Excel;
import com.example.demo.common.exception.ExcelProcessException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Excel 导入导出工具，支持分页导出与安全导入。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
public final class ExcelTool {

    public static final String XLSX_SUFFIX = ".xlsx";
    private static final String DEFAULT_SHEET = "Sheet1";
    private static final int DEFAULT_EXPORT_PAGE_SIZE = 1000;
    private static final int DEFAULT_ROW_WINDOW_SIZE = 200;
    private static final boolean DEFAULT_USE_SHARED_STRINGS = false;
    private static final boolean DEFAULT_AUTO_SIZE = false;
    private static final int DEFAULT_AUTO_SIZE_MAX_ROWS = 2000;
    private static final boolean DEFAULT_COUNT_ENABLED = true;
    private static final boolean DEFAULT_COMPRESS_TEMP_FILES = true;
    private static final long MAX_IMPORT_FILE_SIZE = 50L * 1024 * 1024;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DataFormatter DATA_FORMATTER = new DataFormatter();
    private static final Cache<Class<?>, List<FieldMeta>> FIELD_CACHE = Caffeine.newBuilder().maximumSize(256).weakKeys().build();
    @Getter
    private static volatile int defaultExportPageSize = DEFAULT_EXPORT_PAGE_SIZE;
    @Getter
    private static volatile int defaultRowWindowSize = DEFAULT_ROW_WINDOW_SIZE;
    private static volatile boolean defaultUseSharedStrings = DEFAULT_USE_SHARED_STRINGS;
    @Getter
    @Setter
    private static volatile boolean defaultAutoSize = DEFAULT_AUTO_SIZE;
    @Getter
    private static volatile int defaultAutoSizeMaxRows = DEFAULT_AUTO_SIZE_MAX_ROWS;
    /**
     * -- GETTER --
     *  获取默认是否启用 COUNT 查询。
     *
     * @return true 表示启用 COUNT
     *
     * -- SETTER --
     *  设置默认是否启用 COUNT 查询。
     *
     * @param countEnabled 是否启用 COUNT
     *

     */
    @Setter
    @Getter
    private static volatile boolean defaultCountEnabled = DEFAULT_COUNT_ENABLED;
    @Getter
    @Setter
    private static volatile boolean defaultCompressTempFiles = DEFAULT_COMPRESS_TEMP_FILES;

    /**
     * 工具类禁止实例化。
     *
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private ExcelTool() {
    }

    /**
     * 导出到内存流（默认 Sheet），用于 Web 响应。
     *
     * @param data 导出数据
     * @param type 实体类型
     * @param <T>  实体类型
     * @return 内存流
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public static <T> ByteArrayOutputStream exportToStream(List<T> data, Class<T> type) {
        return exportToStream(data, type, DEFAULT_SHEET);
    }

    /**
     * 导出到内存流（指定 Sheet）。
     *
     * @param data      导出数据
     * @param type      实体类型
     * @param sheetName Sheet 名称
     * @param <T>       实体类型
     * @return 内存流
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public static <T> ByteArrayOutputStream exportToStream(List<T> data, Class<T> type, String sheetName) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exportToStream(data, type, sheetName, outputStream);
        return outputStream;
    }

    /**
     * 导出到指定输出流。
     *
     * @param data         导出数据
     * @param type         实体类型
     * @param sheetName    Sheet 名称
     * @param outputStream 输出流
     * @param <T>          实体类型
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public static <T> void exportToStream(List<T> data, Class<T> type, String sheetName, OutputStream outputStream) {
        Objects.requireNonNull(type, "导出的实体类型不能为空");
        Objects.requireNonNull(outputStream, "输出流不能为空");
        if (data == null) {
            data = new ArrayList<>();
        }
        List<FieldMeta> fieldMetas = getFieldMetas(type);
        try (SXSSFWorkbook workbook = createWorkbook()) {
            CreationHelper creationHelper = workbook.getCreationHelper();
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(creationHelper.createDataFormat().getFormat("yyyy-mm-dd hh:mm:ss"));

            SXSSFSheet sheet = workbook.createSheet(StringUtils.defaultIfBlank(sheetName, DEFAULT_SHEET));
            prepareSheetForExport(sheet);
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

            autoSizeColumns(sheet, fieldMetas.size(), data.size(), defaultAutoSize, defaultAutoSizeMaxRows);

            workbook.write(outputStream);
            outputStream.flush();
        } catch (IOException e) {
            throw new ExcelProcessException("导出 Excel 失败", e);
        }
    }

    /**
     * 分页导出到内存流，避免一次性加载所有数据。
     *
     * @param query        分页查询方法
     * @param type         实体类型
     * @param sheetName    Sheet 名称
     * @param outputStream 输出流
     * @param <T>          实体类型
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public static <T> void exportToStreamByPaging(Function<Page<T>, IPage<T>> query, Class<T> type, String sheetName, OutputStream outputStream) {
        exportToStreamByPaging(query, type, sheetName, outputStream, defaultExportPageSize);
    }

    /**
     * 分页导出到内存流（指定分页大小）。
     *
     * @param query        分页查询方法
     * @param type         实体类型
     * @param sheetName    Sheet 名称
     * @param outputStream 输出流
     * @param pageSize     分页大小
     * @param <T>          实体类型
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public static <T> void exportToStreamByPaging(Function<Page<T>, IPage<T>> query, Class<T> type, String sheetName, OutputStream outputStream, int pageSize) {
        Objects.requireNonNull(query, "分页查询方法不能为空");
        Objects.requireNonNull(type, "导出的实体类型不能为空");
        Objects.requireNonNull(outputStream, "输出流不能为空");
        if (pageSize <= 0) {
            throw new ExcelProcessException("分页大小必须大于 0");
        }
        List<FieldMeta> fieldMetas = getFieldMetas(type);
        try (SXSSFWorkbook workbook = createWorkbook()) {
            CreationHelper creationHelper = workbook.getCreationHelper();
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(creationHelper.createDataFormat().getFormat("yyyy-mm-dd hh:mm:ss"));

            SXSSFSheet sheet = workbook.createSheet(StringUtils.defaultIfBlank(sheetName, DEFAULT_SHEET));
            prepareSheetForExport(sheet);
            buildHeaderRow(sheet, fieldMetas);

            int rowIndex = 1;
            int totalRows = 0;
            int pageNum = 1;
            while (true) {
                IPage<T> pageResult = selectPage(query, pageNum, pageSize);
                List<T> pageData = pageResult.getRecords() == null ? Collections.emptyList() : pageResult.getRecords();
                if (pageData.isEmpty()) {
                    break;
                }
                for (T item : pageData) {
                    Row row = sheet.createRow(rowIndex++);
                    writeDataRow(row, fieldMetas, item, dateStyle);
                    totalRows++;
                }
                if (pageData.size() < pageSize) {
                    break;
                }
                if (pageResult.getPages() > 0 && pageResult.getCurrent() >= pageResult.getPages()) {
                    break;
                }
                pageNum++;
            }

            autoSizeColumns(sheet, fieldMetas.size(), totalRows, defaultAutoSize, defaultAutoSizeMaxRows);

            workbook.write(outputStream);
            outputStream.flush();
        } catch (IOException e) {
            throw new ExcelProcessException("导出 Excel 失败", e);
        }
    }

    /**
     * 导出到指定目录文件。
     *
     * @param data      导出数据
     * @param type      实体类型
     * @param directory 目标目录
     * @param fileName  文件名
     * @param <T>       实体类型
     * @return 导出后的文件
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public static <T> File exportToFile(List<T> data, Class<T> type, Path directory, String fileName) {
        Objects.requireNonNull(directory, "导出目录不能为空");
        Objects.requireNonNull(fileName, "文件名不能为空");
        String targetName = ensureXlsxSuffix(fileName);
        try {
            Files.createDirectories(directory);
            Path target = directory.resolve(targetName);
            try (OutputStream outputStream = Files.newOutputStream(target)) {
                exportToStream(data, type, null, outputStream);
            }
            return target.toFile();
        } catch (IOException e) {
            throw new ExcelProcessException("写入 Excel 文件失败", e);
        }
    }

    /**
     * 从本地文件导入（表头行索引默认 0）。
     *
     * @param file 导入文件
     * @param type 实体类型
     * @param <T>  实体类型
     * @return 导入结果
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public static <T> List<T> importFromFile(File file, Class<T> type) {
        return importFromFile(file, type, 0);
    }

    /**
     * 从本地文件导入。
     *
     * @param file           导入文件
     * @param type           实体类型
     * @param headerRowIndex 表头行索引
     * @param <T>            实体类型
     * @return 导入结果
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public static <T> List<T> importFromFile(File file, Class<T> type, int headerRowIndex) {
        Objects.requireNonNull(file, "导入文件不能为空");
        if (!file.exists()) {
            throw new ExcelProcessException("导入文件不存在: " + file.getAbsolutePath());
        }
        if (file.length() > MAX_IMPORT_FILE_SIZE) {
            throw new ExcelProcessException("导入文件过大，已拒绝处理");
        }
        try (InputStream inputStream = Files.newInputStream(file.toPath())) {
            return importFromStream(inputStream, type, headerRowIndex);
        } catch (IOException e) {
            throw new ExcelProcessException("读取导入文件失败", e);
        }
    }

    /**
     * 从上传文件导入（表头行索引默认 0）。
     *
     * @param multipartFile 上传文件
     * @param type          实体类型
     * @param <T>           实体类型
     * @return 导入结果
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public static <T> List<T> importFromMultipart(MultipartFile multipartFile, Class<T> type) {
        return importFromMultipart(multipartFile, type, 0);
    }

    /**
     * 从上传文件导入。
     *
     * @param multipartFile  上传文件
     * @param type           实体类型
     * @param headerRowIndex 表头行索引
     * @param <T>            实体类型
     * @return 导入结果
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public static <T> List<T> importFromMultipart(MultipartFile multipartFile, Class<T> type, int headerRowIndex) {
        Objects.requireNonNull(multipartFile, "上传文件不能为空");
        if (multipartFile.isEmpty()) {
            throw new ExcelProcessException("上传的 Excel 文件为空");
        }
        if (multipartFile.getSize() > MAX_IMPORT_FILE_SIZE) {
            throw new ExcelProcessException("上传的 Excel 文件过大，已拒绝处理");
        }
        try (InputStream inputStream = multipartFile.getInputStream()) {
            return importFromStream(inputStream, type, headerRowIndex);
        } catch (IOException e) {
            throw new ExcelProcessException("读取上传的 Excel 文件失败", e);
        }
    }

    /**
     * 从输入流导入 Excel 数据。
     *
     * @param inputStream    输入流
     * @param type           实体类型
     * @param headerRowIndex 表头行索引
     * @param <T>            实体类型
     * @return 导入结果
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
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

    /**
     * 构建表头行。
     *
     * @param sheet      Sheet
     * @param fieldMetas 字段元数据
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private static void buildHeaderRow(Sheet sheet, List<FieldMeta> fieldMetas) {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < fieldMetas.size(); i++) {
            Cell cell = headerRow.createCell(i, CellType.STRING);
            cell.setCellValue(fieldMetas.get(i).getHeaderName());
        }
    }

    /**
     * 写入一行数据。
     *
     * @param row        行对象
     * @param fieldMetas 字段元数据
     * @param item       行数据
     * @param dateStyle  日期样式
     * @param <T>        实体类型
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private static <T> void writeDataRow(Row row, List<FieldMeta> fieldMetas, T item, CellStyle dateStyle) {
        if (item == null || row == null) {
            return;
        }
        for (int i = 0; i < fieldMetas.size(); i++) {
            FieldMeta meta = fieldMetas.get(i);
            Object value = meta.getValue(item);
            if (value == null) {
                continue;
            }
            Cell cell = row.createCell(i);
            setCellValue(cell, value, dateStyle);
        }
    }

    /**
     * 将值写入单元格，支持日期与数字类型。
     *
     * @param cell      单元格
     * @param value     值
     * @param dateStyle 日期样式
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
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

    /**
     * 获取字段元数据（带缓存）。
     *
     * @param type 实体类型
     * @return 字段元数据列表
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private static List<FieldMeta> getFieldMetas(Class<?> type) {
        List<FieldMeta> metas = FIELD_CACHE.get(type, ExcelTool::resolveFieldMetas);
        if (metas == null) {
            throw new ExcelProcessException("无法解析 Excel 字段元数据");
        }
        return metas;
    }

    /**
     * 解析实体字段上的 Excel 注解。
     *
     * @param type 实体类型
     * @return 字段元数据列表
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private static List<FieldMeta> resolveFieldMetas(Class<?> type) {
        List<FieldMeta> metas = new ArrayList<>();
        int index = 0;
        for (java.lang.reflect.Field field : collectFields(type)) {
            Excel annotation = field.getAnnotation(Excel.class);
            if (annotation == null) {
                continue;
            }
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if ("serialVersionUID".equals(field.getName())) {
                continue;
            }
            String headerName = resolveHeaderName(annotation, field.getName());
            Map<String, String> exportMapping = parseMapping(annotation);
            FieldMeta meta = new FieldMeta(field, headerName, exportMapping, annotation.sort(), annotation.sort() != Integer.MIN_VALUE, index);
            if (metas.stream().anyMatch(m -> m.getHeaderName().equalsIgnoreCase(headerName))) {
                throw new ExcelProcessException("表头重复: " + headerName);
            }
            metas.add(meta);
            index++;
        }
        metas.sort((a, b) -> {
            if (a.isOrdered() != b.isOrdered()) {
                return a.isOrdered() ? -1 : 1;
            }
            if (a.isOrdered()) {
                int order = Integer.compare(a.getOrder(), b.getOrder());
                return order != 0 ? order : Integer.compare(a.getIndex(), b.getIndex());
            }
            return Integer.compare(a.getIndex(), b.getIndex());
        });
        if (metas.isEmpty()) {
            throw new ExcelProcessException("实体类没有可导入导出的字段");
        }
        return metas;
    }

    private static String resolveHeaderName(Excel annotation, String fallback) {
        if (annotation == null) {
            return fallback;
        }
        String value = annotation.value();
        if (StringUtils.isNotBlank(value)) {
            return value.trim();
        }
        String header = annotation.header();
        if (StringUtils.isNotBlank(header)) {
            return header.trim();
        }
        return fallback;
    }

    /**
     * 递归收集实体及其父类字段。
     *
     * @param type 实体类型
     * @return 字段列表
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private static List<java.lang.reflect.Field> collectFields(Class<?> type) {
        List<java.lang.reflect.Field> fields = new ArrayList<>();
        if (type == null || Object.class.equals(type)) {
            return fields;
        }
        fields.addAll(collectFields(type.getSuperclass()));
        fields.addAll(Arrays.asList(type.getDeclaredFields()));
        return fields;
    }

    /**
     * 读取 Sheet 内容并映射到实体列表。
     *
     * @param sheet          Sheet
     * @param fieldMetas     字段元数据
     * @param type           实体类型
     * @param headerRowIndex 表头行索引
     * @param <T>            实体类型
     * @return 实体列表
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
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

    /**
     * 通过 MyBatis-Plus 分页查询并返回一页数据。
     *
     * @param query    分页查询方法
     * @param pageNum  页码
     * @param pageSize 分页大小
     * @param <T>      实体类型
     * @return 当前页结果
     * @author GPT-5.2-codex(high)
     * @date 2026/2/14
     */
    private static <T> IPage<T> selectPage(Function<Page<T>, IPage<T>> query, int pageNum, int pageSize) {
        Page<T> page = new Page<>(pageNum, pageSize, defaultCountEnabled);
        IPage<T> result = query.apply(page);
        if (result == null) {
            Page<T> empty = new Page<>(pageNum, pageSize);
            empty.setRecords(Collections.emptyList());
            empty.setTotal(0L);
            return empty;
        }
        List<T> records = result.getRecords();
        if (records != null && records.size() > pageSize) {
            throw new ExcelProcessException("分页查询未生效，请确认查询方法支持分页");
        }
        return result;
    }

    /**
     * 根据表头映射字段元数据。
     *
     * @param headerRow  表头行
     * @param fieldMetas 字段元数据
     * @return 列索引 -> 字段元数据 映射
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
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

    /**
     * 从表头单元格中构建列映射。
     *
     * @param headerRow     表头行
     * @param headerMap     表头名 -> 字段元数据
     * @param columnMapping 列索引 -> 字段元数据
     * @param mappedFields  已映射字段集合
     * @param i             列索引
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
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

    /**
     * 读取表头单元格并规范化为匹配键。
     *
     * @param cell 表头单元格
     * @return 小写表头键
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private static String toHeaderKey(Cell cell) {
        Object value = readCellValue(cell);
        if (value == null) {
            return null;
        }
        return value.toString().trim().toLowerCase(Locale.ROOT);
    }

    /**
     * 读取单元格原始值。
     *
     * @param cell 单元格
     * @return 单元格值
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
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

    /**
     * 根据目标类型读取单元格值（字符串类型走格式化）。
     *
     * @param cell       单元格
     * @param targetType 目标类型
     * @return 单元格值
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private static Object readCellValueForType(Cell cell, Class<?> targetType) {
        if (cell == null) {
            return null;
        }
        if (targetType == String.class) {
            return formatCellValue(cell);
        }
        return readCellValue(cell);
    }

    /**
     * 读取单元格值的兜底方案（与主方案互斥）。
     *
     * @param cell       单元格
     * @param targetType 目标类型
     * @return 单元格值
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private static Object readCellValueFallback(Cell cell, Class<?> targetType) {
        if (cell == null) {
            return null;
        }
        if (targetType == String.class) {
            return readCellValue(cell);
        }
        return formatCellValue(cell);
    }

    /**
     * 读取单元格格式化文本。
     *
     * @param cell 单元格
     * @return 格式化字符串
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private static String formatCellValue(Cell cell) {
        String formatted = DATA_FORMATTER.formatCellValue(cell);
        if (StringUtils.isBlank(formatted)) {
            return null;
        }
        return formatted;
    }

    /**
     * 读取公式单元格的缓存结果。
     *
     * @param cell 单元格
     * @return 缓存值
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
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

    /**
     * 将单元格值转换为目标类型。
     *
     * @param rawValue   原始值
     * @param targetType 目标类型
     * @return 转换后的值
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
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

    /**
     * 反射创建实体实例。
     *
     * @param type 实体类型
     * @param <T>  实体类型
     * @return 实体实例
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private static <T> T newInstance(Class<T> type) {
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new ExcelProcessException("创建实体实例失败，请确保存在无参构造函数", e);
        }
    }

    /**
     * 自动调整列宽（可限制最大行数）。
     *
     * @param sheet       Sheet
     * @param columnCount 列数
     * @param rowCount    行数
     * @param autoSize    是否启用自动列宽
     * @param maxRows     自动列宽最大行数
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private static void autoSizeColumns(Sheet sheet, int columnCount, int rowCount, boolean autoSize, int maxRows) {
        if (!autoSize || sheet == null || columnCount <= 0) {
            return;
        }
        if (maxRows > 0 && rowCount > maxRows) {
            return;
        }
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
            int currentWidth = sheet.getColumnWidth(i);
            sheet.setColumnWidth(i, Math.min(currentWidth + 512, 255 * 256));
        }
    }

    /**
     * 确保文件名包含 .xlsx 后缀。
     *
     * @param fileName 文件名
     * @return 追加后缀后的文件名
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public static String ensureXlsxSuffix(String fileName) {
        String trimmed = fileName.trim();
        // 忽略大小写判断结尾是否是 .xlsx
        if (trimmed.toLowerCase(Locale.ROOT).endsWith(XLSX_SUFFIX)) {
            return trimmed;
        }
        return trimmed + XLSX_SUFFIX;
    }

    /**
     * 设置默认分页大小。
     *
     * @param pageSize 分页大小
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public static void setDefaultExportPageSize(int pageSize) {
        if (pageSize <= 0) {
            throw new ExcelProcessException("分页大小必须大于 0");
        }
        defaultExportPageSize = pageSize;
    }

    /**
     * 设置导出内存窗口大小。
     *
     * @param rowWindowSize 内存窗口大小
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public static void setDefaultRowWindowSize(int rowWindowSize) {
        if (rowWindowSize <= 0) {
            throw new ExcelProcessException("内存窗口大小必须大于 0");
        }
        defaultRowWindowSize = rowWindowSize;
    }

    /**
     * 获取共享字符串表开关。
     *
     * @return true 表示启用共享字符串表
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public static boolean isDefaultUseSharedStringsTable() {
        return defaultUseSharedStrings;
    }

    /**
     * 设置共享字符串表开关。
     *
     * @param useSharedStrings 是否启用共享字符串表
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public static void setDefaultUseSharedStringsTable(boolean useSharedStrings) {
        defaultUseSharedStrings = useSharedStrings;
    }

    /**
     * 设置自动列宽最大行数（0 表示不限制）。
     *
     * @param maxRows 最大行数
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public static void setDefaultAutoSizeMaxRows(int maxRows) {
        if (maxRows < 0) {
            throw new ExcelProcessException("自动列宽最大行数不能小于 0");
        }
        defaultAutoSizeMaxRows = maxRows;
    }

    /**
     * 创建 SXSSFWorkbook 并应用默认导出参数。
     *
     * @return SXSSFWorkbook
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private static SXSSFWorkbook createWorkbook() {
        SXSSFWorkbook workbook = new SXSSFWorkbook(new XSSFWorkbook(), defaultRowWindowSize, defaultCompressTempFiles, defaultUseSharedStrings);
        workbook.setCompressTempFiles(defaultCompressTempFiles);
        return workbook;
    }

    /**
     * 导出前初始化 Sheet（如开启自动列宽跟踪）。
     *
     * @param sheet Sheet
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private static void prepareSheetForExport(SXSSFSheet sheet) {
        if (sheet == null) {
            return;
        }
        if (defaultAutoSize) {
            sheet.trackAllColumnsForAutoSizing();
        }
    }

    /**
     * 解析 Excel.mapping 规则为映射表。
     *
     * @param annotation Excel 注解
     * @return 映射表
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private static Map<String, String> parseMapping(Excel annotation) {
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
     *
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private static class FieldMeta {
        @Getter
        private final java.lang.reflect.Field field;
        @Getter
        private final String headerName;
        @Getter
        private final int order;
        @Getter
        private final boolean ordered;
        @Getter
        private final int index;
        private final Map<String, String> exportMapping;
        private final Map<String, String> importMapping;

        /**
         * 构建字段元数据。
         *
         * @param field         字段
         * @param headerName    表头名称
         * @param exportMapping 导出映射
         * @author GPT-5.2-codex(high)
         * @date 2026/2/9
         */
        FieldMeta(java.lang.reflect.Field field, String headerName, Map<String, String> exportMapping,
                  int order, boolean ordered, int index) {
            this.field = field;
            this.headerName = headerName;
            this.order = order;
            this.ordered = ordered;
            this.index = index;
            this.exportMapping = exportMapping;
            this.importMapping = exportMapping.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey, (a, b) -> a, LinkedHashMap::new));
            this.field.setAccessible(true);
        }

        /**
         * 读取字段值并应用导出映射。
         *
         * @param target 目标对象
         * @return 字段值
         * @author GPT-5.2-codex(high)
         * @date 2026/2/9
         */
        Object getValue(Object target) {
            try {
                Object original = field.get(target);
                return applyExportMapping(original);
            } catch (IllegalAccessException e) {
                throw new ExcelProcessException("读取字段失败: " + field.getName(), e);
            }
        }

        /**
         * 写入字段值。
         *
         * @param target 目标对象
         * @param value  字段值
         * @author GPT-5.2-codex(high)
         * @date 2026/2/9
         */
        void setValue(Object target, Object value) {
            try {
                field.set(target, value);
            } catch (IllegalAccessException e) {
                throw new ExcelProcessException("写入字段失败: " + field.getName(), e);
            }
        }

        /**
         * 应用导出映射规则。
         *
         * @param original 原始值
         * @return 映射后的值
         * @author GPT-5.2-codex(high)
         * @date 2026/2/9
         */
        Object applyExportMapping(Object original) {
            if (original == null || exportMapping.isEmpty()) {
                return original;
            }
            String mapped = resolveMapping(exportMapping, original);
            return mapped != null ? mapped : original;
        }

        /**
         * 应用导入映射规则。
         *
         * @param raw 原始值
         * @return 映射后的值
         * @author GPT-5.2-codex(high)
         * @date 2026/2/9
         */
        Object applyImportMapping(Object raw) {
            if (raw == null || importMapping.isEmpty()) {
                return raw;
            }
            String mapped = resolveMapping(importMapping, raw);
            return mapped != null ? mapped : raw;
        }

        /**
         * 根据映射表解析值。
         *
         * @param mapping 映射表
         * @param raw     原始值
         * @return 映射结果
         * @author GPT-5.2-codex(high)
         * @date 2026/2/9
         */
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

        /**
         * 规范化数字键，避免 1 与 1.0 不一致。
         *
         * @param raw 原始值
         * @return 规范化后的字符串
         * @author GPT-5.2-codex(high)
         * @date 2026/2/9
         */
        private String normalizeNumberKey(Object raw) {
            try {
                return new BigDecimal(raw.toString()).stripTrailingZeros().toPlainString();
            } catch (NumberFormatException ignore) {
                return raw.toString();
            }
        }
    }
}
