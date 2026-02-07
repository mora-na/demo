package com.example.demo.framework.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {

    private Jwt jwt = new Jwt();

    private Captcha captcha = new Captcha();

    private Password password = new Password();

    private Filter filter = new Filter();

    @Data
    public static class Jwt {
        private String secret = "change-me";
        private long ttlSeconds = 7200;
    }

    @Data
    public static class Captcha {
        private int width = 120;
        private int height = 40;
        private int codeLength = 4;
        private int thickness = 2;
        private int expireSeconds = 120;
    }

    @Data
    public static class Password {
        private String mode = "plain";
        private String salt = "";
    }

    @Data
    public static class Filter {
        private boolean enabled = true;
        private List<String> excludePaths = new ArrayList<>(
                Arrays.asList("/auth/**", "/error", "/druid/**", "/camunda/**", "/engine-rest/**")
        );
    }
}
