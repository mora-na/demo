package com.example.demo.common.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * 全局 Mapper 扫描，避免在应用入口维护模块清单。
 */
@Configuration
@MapperScan(basePackages = "com.example.demo")
public class MapperScanConfig {
}
