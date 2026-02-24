package com.example.demo.config.resolver;

import com.example.demo.config.support.ConfigValue;

/**
 * 配置解析器接口。
 */
public interface ConfigResolver {

    ConfigValue resolve(ConfigResolveRequest request);
}
