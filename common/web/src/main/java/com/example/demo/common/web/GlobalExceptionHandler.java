package com.example.demo.common.web;

import com.example.demo.common.config.CommonConstants;
import com.example.demo.common.exception.ExcelProcessException;
import com.example.demo.common.i18n.I18nService;
import com.example.demo.common.model.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final I18nService i18nService;
    private final CommonConstants commonConstants;
    private final AtomicLong clientAbortWindowStart = new AtomicLong(0L);
    private final AtomicInteger clientAbortCounter = new AtomicInteger(0);
    private final long clientAbortWindowMillis;
    private final int clientAbortWarnThreshold;
    private final int clientAbortMessageMaxLength;

    public GlobalExceptionHandler(I18nService i18nService, CommonConstants commonConstants) {
        this.i18nService = i18nService;
        this.commonConstants = commonConstants;
        CommonConstants.ExceptionHandling exceptionHandling = commonConstants.getExceptionHandling();
        this.clientAbortWindowMillis = exceptionHandling.getClientAbortWindowMillis();
        this.clientAbortWarnThreshold = exceptionHandling.getClientAbortWarnThreshold();
        this.clientAbortMessageMaxLength = exceptionHandling.getClientAbortMessageMaxLength();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public CommonResult<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String message = resolveBindingMessage(ex.getBindingResult());
        return CommonResult.error(HttpStatus.BAD_REQUEST.value(), message);
    }

    @ExceptionHandler(BindException.class)
    public CommonResult<Void> handleBindException(BindException ex) {
        String message = resolveBindingMessage(ex.getBindingResult());
        return CommonResult.error(HttpStatus.BAD_REQUEST.value(), message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public CommonResult<Void> handleConstraintViolation(ConstraintViolationException ex) {
        String message = resolveConstraintViolationMessage(ex);
        return CommonResult.error(HttpStatus.BAD_REQUEST.value(), message);
    }

    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            HttpRequestMethodNotSupportedException.class,
            HttpMessageNotReadableException.class
    })
    public CommonResult<Void> handleBadRequest(Exception ex) {
        return CommonResult.error(HttpStatus.BAD_REQUEST.value(), i18nService.getMessage("common.request.invalid"));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public CommonResult<Void> handleNotFound(NoHandlerFoundException ex) {
        return CommonResult.error(HttpStatus.NOT_FOUND.value(), i18nService.getMessage("common.request.invalid"));
    }

    @ExceptionHandler(ExcelProcessException.class)
    public CommonResult<Void> handleExcelProcess(ExcelProcessException ex) {
        return CommonResult.error(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                i18nService.getMessage("excel.process.error"));
    }

    @ExceptionHandler(Exception.class)
    public CommonResult<Void> handleException(Exception ex, HttpServletRequest request) {
        if (isClientAbort(ex)) {
            recordClientAbort(ex, request);
            return null;
        }
        log.error("Unhandled exception", ex);
        return CommonResult.error(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                i18nService.getMessage("common.internal.error"));
    }

    private String resolveBindingMessage(BindingResult bindingResult) {
        if (bindingResult == null || bindingResult.getAllErrors().isEmpty()) {
            return i18nService.getMessage("common.validation.failed");
        }
        ObjectError error = bindingResult.getAllErrors().get(0);
        String message = error.getDefaultMessage();
        if (message == null || message.trim().isEmpty()) {
            return i18nService.getMessage("common.validation.failed");
        }
        return message;
    }

    private String resolveConstraintViolationMessage(ConstraintViolationException ex) {
        if (ex == null || ex.getConstraintViolations() == null || ex.getConstraintViolations().isEmpty()) {
            return i18nService.getMessage("common.validation.failed");
        }
        ConstraintViolation<?> violation = ex.getConstraintViolations().iterator().next();
        String message = violation == null ? null : violation.getMessage();
        if (message == null || message.trim().isEmpty()) {
            return i18nService.getMessage("common.validation.failed");
        }
        return message;
    }

    private boolean isClientAbort(Throwable ex) {
        Throwable current = ex;
        while (current != null) {
            String className = current.getClass().getName();
            if ("org.apache.catalina.connector.ClientAbortException".equals(className)) {
                return true;
            }
            if (current instanceof java.io.IOException) {
                String message = current.getMessage();
                if (message != null) {
                    String lower = message.toLowerCase(Locale.ROOT);
                    if (lower.contains("broken pipe")
                            || lower.contains("connection reset")
                            || lower.contains("connection aborted")) {
                        return true;
                    }
                }
            }
            current = current.getCause();
        }
        return false;
    }

    private void recordClientAbort(Throwable ex, HttpServletRequest request) {
        long now = System.currentTimeMillis();
        long start = clientAbortWindowStart.get();
        if (start == 0L || now - start > clientAbortWindowMillis) {
            if (clientAbortWindowStart.compareAndSet(start, now)) {
                clientAbortCounter.set(0);
            }
        }
        int count = clientAbortCounter.incrementAndGet();
        if (clientAbortWarnThreshold > 0 && count == clientAbortWarnThreshold) {
            log.warn("Client disconnects detected: {} within {}ms, lastPath={}, lastMessage={}",
                    count, clientAbortWindowMillis, safePath(request), safeMessage(ex));
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Client disconnected: path={}, message={}", safePath(request), safeMessage(ex));
        }
    }

    private String safePath(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        String uri = request.getRequestURI();
        return uri == null ? "" : uri;
    }

    private String safeMessage(Throwable ex) {
        if (ex == null || ex.getMessage() == null) {
            return "";
        }
        String message = ex.getMessage();
        int maxLength = clientAbortMessageMaxLength;
        if (maxLength <= 0) {
            return message;
        }
        return message.length() > maxLength ? message.substring(0, maxLength) + "...(truncated)" : message;
    }
}
