package com.example.demo.common.web;

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

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final I18nService i18nService;

    public GlobalExceptionHandler(I18nService i18nService) {
        this.i18nService = i18nService;
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
    public CommonResult<Void> handleException(Exception ex) {
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
}
