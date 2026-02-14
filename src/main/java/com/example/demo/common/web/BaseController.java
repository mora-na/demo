package com.example.demo.common.web;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.exception.ExcelProcessException;
import com.example.demo.common.i18n.I18nService;
import com.example.demo.common.model.CommonResult;
import com.example.demo.common.model.PageQuery;
import com.example.demo.common.model.PageResult;
import com.example.demo.common.tool.ExcelTool;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 控制器基类，提供分页与统一响应封装等通用能力。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
public abstract class BaseController {

    @org.springframework.beans.factory.annotation.Autowired
    private I18nService i18nService;

    /**
     * 执行分页查询并返回分页结果。
     *
     * @param select 分页查询函数
     * @param <T>    数据类型
     * @return 分页结果
     * @author GPT-5.2-codex(high)
     * @date 2026/2/14
     */
    protected <T> PageResult<T> page(Function<Page<T>, IPage<T>> select) {
        IPage<T> page = select.apply(buildPage());
        return toPageResult(page);
    }

    /**
     * 执行分页查询并将实体转换为视图对象。
     *
     * @param select    分页查询函数
     * @param converter 实体到视图转换器
     * @param <E>       实体类型
     * @param <V>       视图类型
     * @return 分页结果
     * @author GPT-5.2-codex(high)
     * @date 2026/2/14
     */
    protected <E, V> PageResult<V> page(Function<Page<E>, IPage<E>> select, Function<E, V> converter) {
        IPage<E> page = select.apply(buildPage());
        return toPageResult(page, converter);
    }

    /**
     * 按查询参数执行分页查询并转换结果。
     *
     * @param query     查询参数
     * @param select    分页查询函数
     * @param converter 实体到视图转换器
     * @param <Q>       查询参数类型
     * @param <E>       实体类型
     * @param <V>       视图类型
     * @return 分页结果
     * @author GPT-5.2-codex(high)
     * @date 2026/2/14
     */
    protected <Q, E, V> PageResult<V> page(Q query, BiFunction<Page<E>, Q, IPage<E>> select, Function<E, V> converter) {
        return page(page -> select.apply(page, query), converter);
    }

    /**
     * 按查询参数执行分页查询并返回分页结果。
     */
    protected <Q, E> PageResult<E> page(Q query, BiFunction<Page<E>, Q, IPage<E>> select) {
        return page(page -> select.apply(page, query));
    }

    /**
     * 构建空分页结果（保留请求分页信息）。
     */
    protected <T> PageResult<T> emptyPage() {
        PageQuery query = getPageQuery();
        int pageNum = query.getPageNum() == null ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null ? 10 : query.getPageSize();
        return new PageResult<>(0L, Collections.emptyList(), pageNum, pageSize);
    }

    /**
     * 构造成功响应（无数据体）。
     *
     * @param <T> 数据类型
     * @return 成功响应
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    protected <T> CommonResult<T> success() {
        return CommonResult.success(i18n("common.success"));
    }

    /**
     * 构造成功响应（带数据体）。
     *
     * @param data 数据体
     * @param <T>  数据类型
     * @return 成功响应
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    protected <T> CommonResult<T> success(T data) {
        return CommonResult.success(i18n("common.success"), data);
    }

    /**
     * 构造成功响应（自定义消息）。
     *
     * @param message 提示信息
     * @param <T>     数据类型
     * @return 成功响应
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    protected <T> CommonResult<T> success(String message) {
        return CommonResult.success(message);
    }

    /**
     * 构造成功响应（自定义消息与数据体）。
     *
     * @param message 提示信息
     * @param data    数据体
     * @param <T>     数据类型
     * @return 成功响应
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    protected <T> CommonResult<T> success(String message, T data) {
        return CommonResult.success(message, data);
    }

    /**
     * 构造失败响应（默认消息）。
     *
     * @param <T> 数据类型
     * @return 失败响应
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    protected <T> CommonResult<T> error() {
        return CommonResult.error(i18n("common.error"));
    }

    /**
     * 构造失败响应（自定义消息）。
     *
     * @param message 提示信息
     * @param <T>     数据类型
     * @return 失败响应
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    protected <T> CommonResult<T> error(String message) {
        return CommonResult.error(message);
    }

    /**
     * 构造失败响应（自定义状态码与消息）。
     *
     * @param code    状态码
     * @param message 提示信息
     * @param <T>     数据类型
     * @return 失败响应
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    protected <T> CommonResult<T> error(int code, String message) {
        return CommonResult.error(code, message);
    }

    /**
     * 构造失败响应（自定义状态码、消息与数据体）。
     *
     * @param code    状态码
     * @param message 提示信息
     * @param data    数据体
     * @param <T>     数据类型
     * @return 失败响应
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    protected <T> CommonResult<T> error(int code, String message, T data) {
        return CommonResult.error(code, message, data);
    }

    protected String i18n(String key, Object... args) {
        return i18nService == null ? key : i18nService.getMessage(key, args);
    }

    /**
     * 读取请求参数并构建分页参数。
     */
    protected PageQuery getPageQuery() {
        HttpServletRequest req = currentRequest();
        int pageNum = Math.max(parseInt(req.getParameter("pageNum"), 1), 1);
        int pageSize = Math.max(parseInt(req.getParameter("pageSize"), 10), 1);
        pageSize = Math.min(pageSize, 500);
        String orderByColumn = StringUtils.trimToNull(req.getParameter("orderByColumn"));
        String isAsc = StringUtils.trimToNull(req.getParameter("isAsc"));
        return new PageQuery(pageNum, pageSize, orderByColumn, isAsc);
    }

    private <T> Page<T> buildPage() {
        return getPageQuery().buildPage();
    }

    private <T> PageResult<T> toPageResult(IPage<T> page) {
        if (page == null) {
            PageQuery query = getPageQuery();
            int pageNum = query.getPageNum() == null ? 1 : query.getPageNum();
            int pageSize = query.getPageSize() == null ? 10 : query.getPageSize();
            return new PageResult<>(0L, Collections.emptyList(), pageNum, pageSize);
        }
        int pageNum = (int) Math.max(page.getCurrent(), 1);
        int pageSize = (int) Math.max(page.getSize(), 1);
        List<T> records = page.getRecords() == null ? Collections.emptyList() : page.getRecords();
        return new PageResult<>(Math.max(page.getTotal(), 0L), records, pageNum, pageSize);
    }

    private <E, V> PageResult<V> toPageResult(IPage<E> page, Function<E, V> converter) {
        if (page == null) {
            PageQuery query = getPageQuery();
            int pageNum = query.getPageNum() == null ? 1 : query.getPageNum();
            int pageSize = query.getPageSize() == null ? 10 : query.getPageSize();
            return new PageResult<>(0L, Collections.emptyList(), pageNum, pageSize);
        }
        int pageNum = (int) Math.max(page.getCurrent(), 1);
        int pageSize = (int) Math.max(page.getSize(), 1);
        List<V> voList = page.getRecords() == null ? Collections.emptyList()
                : page.getRecords().stream().map(converter).collect(Collectors.toList());
        return new PageResult<>(Math.max(page.getTotal(), 0L), voList, pageNum, pageSize);
    }

    /**
     * 获取当前线程绑定的 HTTP 请求。
     *
     * @return HTTP 请求
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private HttpServletRequest currentRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    }

    /**
     * 将数据导出为 Excel 并写入 HTTP 响应。
     *
     * @param response HTTP 响应
     * @param data     导出数据列表
     * @param type     导出数据类型
     * @param fileName 文件名
     * @param <T>      数据类型
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    protected <T> void exportExcel(HttpServletResponse response, List<T> data, Class<T> type, String fileName) {
        String targetName = normalizeFileName(fileName);
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            String encodedName = URLEncoder.encode(targetName, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedName);
            ExcelTool.exportToStream(data, type, null, response.getOutputStream());
            response.flushBuffer();
        } catch (IOException e) {
            throw new ExcelProcessException("写出 Excel 响应失败", e);
        }
    }

    /**
     * 分页导出 Excel，避免一次性加载所有数据。
     *
     * @param response HTTP 响应
     * @param query    分页查询函数
     * @param type     导出数据类型
     * @param fileName 文件名
     * @param <T>      数据类型
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    protected <T> void exportExcel(HttpServletResponse response, Function<Page<T>, IPage<T>> query, Class<T> type, String fileName) {
        String targetName = normalizeFileName(fileName);
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            String encodedName = URLEncoder.encode(targetName, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedName);
            ExcelTool.exportToStreamByPaging(query, type, null, response.getOutputStream());
            response.flushBuffer();
        } catch (IOException e) {
            throw new ExcelProcessException("写出 Excel 响应失败", e);
        }
    }

    /**
     * 标准化导出文件名并补全后缀。
     *
     * @param fileName 原始文件名
     * @return 标准化文件名
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private String normalizeFileName(String fileName) {
        String fallback = "export" + ExcelTool.XLSX_SUFFIX;
        if (StringUtils.isBlank(fileName)) {
            return fallback;
        }
        String trimmed = fileName.trim();
        return ExcelTool.ensureXlsxSuffix(trimmed);
    }

    /**
     * 解析整数字符串，解析失败返回默认值。
     *
     * @param s   字符串
     * @param def 默认值
     * @return 解析结果
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private int parseInt(String s, int def) {
        try {
            return (s == null) ? def : Integer.parseInt(s);
        } catch (Exception e) {
            return def;
        }
    }

}
