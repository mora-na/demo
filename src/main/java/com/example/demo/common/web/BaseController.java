package com.example.demo.common.web;


import com.example.demo.common.exception.ExcelProcessException;
import com.example.demo.common.i18n.I18nService;
import com.example.demo.common.model.CommonResult;
import com.example.demo.common.model.PageParam;
import com.example.demo.common.model.PageResult;
import com.example.demo.common.tool.ExcelTool;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
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
     * 启动分页上下文，读取请求参数并设置默认/上限值。
     *
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    protected void startPage() {
        HttpServletRequest req = currentRequest();

        int pageNum = parseInt(req.getParameter("pageNum"), 1);
        int pageSize = parseInt(req.getParameter("pageSize"), 10);

        // 可加上最大分页大小限制，防止一次拉太多
        pageSize = Math.min(pageSize, 200);

        PageHelper.startPage(pageNum, pageSize);

    }

    /**
     * 将查询结果转换为分页响应并清理分页上下文。
     *
     * @param list 查询结果列表
     * @param <T>  数据类型
     * @return 分页结果
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    protected <T> PageResult<T> getPageResult(List<T> list) {
        PageInfo<T> pageInfo = new PageInfo<>(list);
        PageResult<T> pageResult = new PageResult<>(pageInfo.getTotal(), list, pageInfo.getPageNum(), pageInfo.getPageSize());
        PageHelper.clearPage();
        return pageResult;
    }


    /**
     * 执行分页查询并返回分页结果。
     *
     * @param select 查询函数
     * @param <T>    数据类型
     * @return 分页结果
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    protected <T> PageResult<T> page(Supplier<List<T>> select) {
        PageParam p = getPageParam();
        PageInfo<T> pageInfo = doPageInfo(p, select);
        return new PageResult<>(pageInfo.getTotal(), pageInfo.getList(), pageInfo.getPageNum(), pageInfo.getPageSize());
    }

    /**
     * 执行分页查询并将实体转换为视图对象。
     *
     * @param select    查询函数
     * @param converter 实体到视图转换器
     * @param <E>       实体类型
     * @param <V>       视图类型
     * @return 分页结果
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    protected <E, V> PageResult<V> page(Supplier<List<E>> select, Function<E, V> converter) {
        PageParam p = getPageParam();
        PageInfo<E> pageInfo = doPageInfo(p, select);
        List<V> voList = pageInfo.getList().stream().map(converter).collect(Collectors.toList());
        return new PageResult<>(pageInfo.getTotal(), voList, pageInfo.getPageNum(), pageInfo.getPageSize());
    }

    /**
     * 按查询参数执行分页查询并转换结果。
     *
     * @param query     查询参数
     * @param select    查询函数
     * @param converter 实体到视图转换器
     * @param <Q>       查询参数类型
     * @param <E>       实体类型
     * @param <V>       视图类型
     * @return 分页结果
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    protected <Q, E, V> PageResult<V> page(Q query, Function<Q, List<E>> select, Function<E, V> converter) {
        return page(() -> select.apply(query), converter);
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
     *
     * @return 分页参数
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private PageParam getPageParam() {
        HttpServletRequest req = currentRequest();
        int pageNum = Math.max(parseInt(req.getParameter("pageNum"), 1), 1);
        int pageSize = Math.max(parseInt(req.getParameter("pageSize"), 10), 1);
        pageSize = Math.min(pageSize, 200);
        return new PageParam(pageNum, pageSize);
    }

    /**
     * 在 PageHelper 环境中执行查询并返回 PageInfo。
     *
     * @param p      分页参数
     * @param select 查询函数
     * @param <E>    数据类型
     * @return PageInfo 结果
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private <E> PageInfo<E> doPageInfo(PageParam p, Supplier<List<E>> select) {
        try (Page<Object> ignored = PageHelper.startPage(p.getPageNum(), p.getPageSize())) {
            return ignored.doSelectPageInfo(select::get);
        } finally {
            PageHelper.clearPage();
        }
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
    protected <T> void exportExcel(HttpServletResponse response, Supplier<List<T>> query, Class<T> type, String fileName) {
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
