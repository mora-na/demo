package com.example.demo.common.web.xss;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * XSS 防护配置项，绑定 security.xss 前缀。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Data
@Component
@ConfigurationProperties(prefix = "security.xss")
public class XssProperties {

    private boolean enabled = true;

    private List<String> excludePaths = new ArrayList<>();

}
