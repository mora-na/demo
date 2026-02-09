package com.example.demo.common.web.permission;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "security.permission")
public class PermissionProperties {

    private String source = "db";

    private boolean enabled = true;

    private boolean requireLoginByDefault = false;

    private List<String> superUsers = new ArrayList<>();

    private Map<String, List<String>> userPermissions = new LinkedHashMap<>();

    private List<String> excludePaths = new ArrayList<>();

    private long cacheSeconds = 0;

}
