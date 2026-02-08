package com.example.demo.common.web.xss;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "security.xss")
public class XssProperties {

    private boolean enabled = true;

    private List<String> excludePaths = new ArrayList<>();

}
