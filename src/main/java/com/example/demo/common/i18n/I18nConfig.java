package com.example.demo.common.i18n;

import com.example.demo.common.config.CommonConstants;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Locale;

@Configuration
public class I18nConfig {

    @Bean
    public MessageSource messageSource(CommonConstants systemConstants) {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        CommonConstants.I18n i18n = systemConstants.getI18n();
        messageSource.setBasename(i18n.getBasename());
        messageSource.setDefaultEncoding(i18n.getDefaultEncoding());
        messageSource.setFallbackToSystemLocale(i18n.isFallbackToSystemLocale());
        messageSource.setUseCodeAsDefaultMessage(i18n.isUseCodeAsDefaultMessage());
        return messageSource;
    }

    @Bean
    public LocaleResolver localeResolver(CommonConstants systemConstants) {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(Locale.forLanguageTag(systemConstants.getI18n().getDefaultLocaleTag()));
        return resolver;
    }

    @Bean
    public LocalValidatorFactoryBean validator(MessageSource messageSource) {
        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
        bean.setValidationMessageSource(messageSource);
        return bean;
    }
}
