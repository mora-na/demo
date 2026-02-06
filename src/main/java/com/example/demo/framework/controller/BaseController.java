package com.example.demo.framework.controller;


import com.example.demo.framework.tools.ExcelProcessException;
import com.example.demo.framework.tools.ExcelTool;
import com.example.demo.framework.web.CommonResult;
import com.example.demo.framework.web.PageParam;
import com.example.demo.framework.web.PageResult;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class BaseController {

    protected void startPage() {
        HttpServletRequest req = currentRequest();

        int pageNum = parseInt(req.getParameter("pageNum"), 1);
        int pageSize = parseInt(req.getParameter("pageSize"), 10);

        // 可加上最大分页大小限制，防止一次拉太多
        pageSize = Math.min(pageSize, 200);

        PageHelper.startPage(pageNum, pageSize);

    }

    protected <T> PageResult<T> getPageResult(List<T> list) {
        PageInfo<T> pageInfo = new PageInfo<>(list);
        PageResult<T> pageResult = new PageResult<>(pageInfo.getTotal(), list, pageInfo.getPageNum(), pageInfo.getPageSize());
        PageHelper.clearPage();
        return pageResult;
    }


    protected <T> PageResult<T> page(Supplier<List<T>> select) {
        PageParam p = getPageParam();
        PageInfo<T> pageInfo = doPageInfo(p, select);
        return new PageResult<>(pageInfo.getTotal(), pageInfo.getList(), pageInfo.getPageNum(), pageInfo.getPageSize());
    }

    protected <E, V> PageResult<V> page(Supplier<List<E>> select, Function<E, V> converter) {
        PageParam p = getPageParam();
        PageInfo<E> pageInfo = doPageInfo(p, select);
        List<V> voList = pageInfo.getList().stream().map(converter).collect(Collectors.toList());
        return new PageResult<>(pageInfo.getTotal(), voList, pageInfo.getPageNum(), pageInfo.getPageSize());
    }

    protected <Q, E, V> PageResult<V> page(Q query, Function<Q, List<E>> select, Function<E, V> converter) {
        return page(() -> select.apply(query), converter);
    }

    protected <T> CommonResult<T> success() {
        return CommonResult.success();
    }

    protected <T> CommonResult<T> success(T data) {
        return CommonResult.success(data);
    }

    protected <T> CommonResult<T> success(String message) {
        return CommonResult.success(message);
    }

    protected <T> CommonResult<T> success(String message, T data) {
        return CommonResult.success(message, data);
    }

    protected <T> CommonResult<T> error() {
        return CommonResult.error();
    }

    protected <T> CommonResult<T> error(String message) {
        return CommonResult.error(message);
    }

    protected <T> CommonResult<T> error(int code, String message) {
        return CommonResult.error(code, message);
    }

    protected <T> CommonResult<T> error(int code, String message, T data) {
        return CommonResult.error(code, message, data);
    }

    private PageParam getPageParam() {
        HttpServletRequest req = currentRequest();
        int pageNum = Math.max(parseInt(req.getParameter("pageNum"), 1), 1);
        int pageSize = Math.max(parseInt(req.getParameter("pageSize"), 10), 1);
        pageSize = Math.min(pageSize, 200);
        return new PageParam(pageNum, pageSize);
    }

    private <E> PageInfo<E> doPageInfo(PageParam p, Supplier<List<E>> select) {
        try (Page<Object> ignored = PageHelper.startPage(p.getPageNum(), p.getPageSize())) {
            return ignored.doSelectPageInfo(select::get);
        } finally {
            PageHelper.clearPage();
        }
    }

    private HttpServletRequest currentRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    }

    /**
     * 将数据导出为 Excel 并写入 Http 响应。
     */
    protected <T> void exportExcel(HttpServletResponse response, List<T> data, Class<T> type, String fileName) {
        String targetName = normalizeFileName(fileName);
        try (ByteArrayOutputStream outputStream = ExcelTool.exportToStream(data, type)) {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            String encodedName = URLEncoder.encode(targetName, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedName);
            response.setContentLength(outputStream.size());
            response.getOutputStream().write(outputStream.toByteArray());
            response.flushBuffer();
        } catch (IOException e) {
            throw new ExcelProcessException("写出 Excel 响应失败", e);
        }
    }

    private String normalizeFileName(String fileName) {
        String fallback = "export" + ExcelTool.XLSX_SUFFIX;
        if (StringUtils.isBlank(fileName)) {
            return fallback;
        }
        String trimmed = fileName.trim();
        return ExcelTool.ensureXlsxSuffix(trimmed);
    }

    private int parseInt(String s, int def) {
        try {
            return (s == null) ? def : Integer.parseInt(s);
        } catch (Exception e) {
            return def;
        }
    }

}
