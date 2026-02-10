package com.example.demo.common.i18n;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

@Component
public class I18nService {

    private final MessageSource messageSource;

    public I18nService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getMessage(String key, Object... args) {
        return getMessage(LocaleContextHolder.getLocale(), key, args);
    }

    public String getMessage(HttpServletRequest request, String key, Object... args) {
        Locale locale = request == null ? LocaleContextHolder.getLocale() : request.getLocale();
        return getMessage(locale, key, args);
    }

    public String getMessage(Locale locale, String key, Object... args) {
        Locale target = locale == null ? LocaleContextHolder.getLocale() : locale;
        return messageSource.getMessage(key, args, key, target);
    }
}
