package com.example.demo.extension.executor;

import com.example.demo.extension.api.executor.DynamicApiExecuteResult;
import com.example.demo.extension.api.request.DynamicApiRequest;
import com.example.demo.extension.config.DynamicApiConstants;
import com.example.demo.extension.model.DynamicApi;
import com.example.demo.extension.model.DynamicApiAuthMode;
import com.example.demo.extension.model.HttpForwardConfig;
import com.example.demo.extension.registry.DynamicApiMeta;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class HttpForwardStrategyTest {

    @Test
    void blockedHostIsRejected() {
        DynamicApiConstants constants = new DynamicApiConstants();
        constants.getHttp().getBlockedHosts().add("blocked.example.com");
        HttpForwardStrategy strategy = new HttpForwardStrategy(constants);

        HttpForwardConfig config = new HttpForwardConfig();
        config.setUrl("http://blocked.example.com/demo");
        config.setMethod("GET");

        try {
            DynamicApiExecuteResult result = strategy.execute(buildContext(config));
            assertFalse(result.isSuccess());
            assertEquals(constants.getMessage().getHttpInvalid(), result.getMessage());
        } finally {
            strategy.closeClient();
        }
    }

    @Test
    void allowedCidrsRejectsNonMatchingHost() {
        DynamicApiConstants constants = new DynamicApiConstants();
        constants.getHttp().getAllowedCidrs().add("192.0.2.0/24");
        constants.getHttp().setBlockPrivateNetwork(false);
        HttpForwardStrategy strategy = new HttpForwardStrategy(constants);

        HttpForwardConfig config = new HttpForwardConfig();
        config.setUrl("http://8.8.8.8/demo");
        config.setMethod("GET");

        try {
            DynamicApiExecuteResult result = strategy.execute(buildContext(config));
            assertFalse(result.isSuccess());
            assertEquals(constants.getMessage().getHttpInvalid(), result.getMessage());
        } finally {
            strategy.closeClient();
        }
    }

    private DynamicApiContext buildContext(HttpForwardConfig config) {
        DynamicApi api = new DynamicApi();
        api.setId(1L);
        api.setPath("/ext/http");
        api.setMethod("GET");
        api.setType("HTTP");
        DynamicApiMeta meta = new DynamicApiMeta(api, "HTTP", DynamicApiAuthMode.INHERIT, config, null);
        DynamicApiRequest request = DynamicApiRequest.builder()
                .path("/ext/http")
                .method("GET")
                .build();
        return new DynamicApiContext(meta, request, 1000L, null, null, null, null);
    }
}
