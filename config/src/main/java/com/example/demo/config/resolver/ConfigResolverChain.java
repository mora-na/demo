package com.example.demo.config.resolver;

import com.example.demo.config.support.ConfigValue;

import java.util.List;

/**
 * 责任链配置解析器。
 */
public class ConfigResolverChain {

    private final List<ConfigResolver> resolvers;

    public ConfigResolverChain(List<ConfigResolver> resolvers) {
        this.resolvers = resolvers;
    }

    public ConfigValue resolve(ConfigResolveRequest request) {
        if (resolvers == null || resolvers.isEmpty()) {
            return null;
        }
        for (ConfigResolver resolver : resolvers) {
            if (resolver == null) {
                continue;
            }
            ConfigValue value = resolver.resolve(request);
            if (value != null) {
                return value;
            }
        }
        return null;
    }
}
