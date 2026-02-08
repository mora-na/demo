package com.example.demo.common.mybatis;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "security.sql-guard")
public class SqlGuardProperties {

    private boolean enabled = true;

    private boolean blockFullTable = true;

    private boolean blockMultiStatement = true;

}
