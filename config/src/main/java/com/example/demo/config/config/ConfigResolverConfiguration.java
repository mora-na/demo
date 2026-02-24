package com.example.demo.config.config;

import com.example.demo.config.mapper.SysConfigMapper;
import com.example.demo.config.resolver.*;
import com.example.demo.config.support.ConfigCacheService;
import com.example.demo.config.support.ConfigCryptoService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.List;

/**
 * 配置解析器链配置。
 */
@Configuration
public class ConfigResolverConfiguration {

    @Bean
    public ConfigResolverChain configResolverChain(Environment environment,
                                                   ConfigConstants constants,
                                                   ConfigCryptoService cryptoService,
                                                   SysConfigMapper configMapper,
                                                   ConfigCacheService cacheService,
                                                   ConfigDefaultsProperties defaultsProperties) {
        List<ConfigResolver> resolvers = Arrays.asList(
                new EnvConfigResolver(environment, constants, cryptoService),
                new DbConfigResolver(configMapper, constants, cacheService, cryptoService),
                new DefaultConfigResolver(defaultsProperties, constants)
        );
        return new ConfigResolverChain(resolvers);
    }
}
