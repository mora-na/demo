package com.example.demo.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * 确保 MVC 的 Jackson 转换器使用统一 ObjectMapper。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/14
 */
@Configuration
public class WebMvcJacksonConfig implements WebMvcConfigurer {

    private final ObjectMapper objectMapper;

    public WebMvcJacksonConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.removeIf(converter -> converter instanceof MappingJackson2HttpMessageConverter);
        MappingJackson2HttpMessageConverter jacksonConverter = new MappingJackson2HttpMessageConverter(objectMapper);
        converters.add(0, jacksonConverter);
    }
}
