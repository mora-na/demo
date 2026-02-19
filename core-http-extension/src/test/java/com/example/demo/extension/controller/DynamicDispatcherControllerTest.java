package com.example.demo.extension.controller;

import com.example.demo.common.config.CommonConstants;
import com.example.demo.common.i18n.I18nService;
import com.example.demo.extension.adapter.RateLimitAdapter;
import com.example.demo.extension.adapter.RateLimitDecision;
import com.example.demo.extension.api.executor.DynamicApiExecuteResult;
import com.example.demo.extension.api.executor.DynamicApiExecutionContext;
import com.example.demo.extension.api.executor.ExecuteStrategy;
import com.example.demo.extension.config.DynamicApiConstants;
import com.example.demo.extension.config.DynamicApiProperties;
import com.example.demo.extension.executor.DynamicApiExecutor;
import com.example.demo.extension.executor.DynamicApiExecutorRouter;
import com.example.demo.extension.executor.ExecuteStrategyFactory;
import com.example.demo.extension.model.DynamicApi;
import com.example.demo.extension.model.DynamicApiAuthMode;
import com.example.demo.extension.model.DynamicApiResponse;
import com.example.demo.extension.registry.DynamicApiMeta;
import com.example.demo.extension.registry.DynamicApiRegistry;
import com.example.demo.extension.support.DynamicApiRequestExtractor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DynamicDispatcherControllerTest {

    private static ThreadPoolTaskExecutor buildExecutor(String prefix) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix(prefix);
        executor.initialize();
        return executor;
    }

    private static ResponseEntity<DynamicApiResponse<Object>> awaitResult(
            DeferredResult<ResponseEntity<DynamicApiResponse<Object>>> deferred) throws InterruptedException {
        for (int i = 0; i < 40; i++) {
            Object result = deferred.getResult();
            if (result instanceof ResponseEntity) {
                @SuppressWarnings("unchecked")
                ResponseEntity<DynamicApiResponse<Object>> response = (ResponseEntity<DynamicApiResponse<Object>>) result;
                return response;
            }
            TimeUnit.MILLISECONDS.sleep(50);
        }
        return null;
    }

    private static void registerMeta(DynamicApiRegistry registry) {
        DynamicApi api = new DynamicApi();
        api.setId(1L);
        api.setPath("/ext/large");
        api.setMethod("GET");
        api.setType("TEST");
        api.setStatus("ENABLED");
        api.setAuthMode("PUBLIC");
        DynamicApiMeta meta = new DynamicApiMeta(api, "TEST", DynamicApiAuthMode.PUBLIC, null, null);
        registry.register(meta);
    }

    @Test
    void responseTooLargeReturnsError() throws Exception {
        DynamicApiConstants constants = new DynamicApiConstants();
        constants.getExecute().setMaxResponseBytes(10);
        DynamicApiProperties properties = new DynamicApiProperties();
        ObjectMapper objectMapper = new ObjectMapper();
        CommonConstants commonConstants = new CommonConstants();

        ExecuteStrategy strategy = new LargeResultStrategy();
        ThreadPoolTaskExecutor taskExecutor = buildExecutor("ext-test-");
        ThreadPoolTaskExecutor cleanupExecutor = buildExecutor("ext-cleanup-test-");
        ScheduledExecutorService cleanupScheduler = Executors.newSingleThreadScheduledExecutor();

        DynamicApiExecutor executor = new DynamicApiExecutor(
                taskExecutor,
                cleanupExecutor,
                cleanupScheduler,
                new ExecuteStrategyFactory(Collections.singletonList(strategy), properties),
                constants,
                new DynamicApiExecutorRouter(taskExecutor, Collections.emptyMap(), properties),
                null,
                null
        );

        DynamicApiRegistry registry = new DynamicApiRegistry();
        registerMeta(registry);

        MessageSource messageSource = new MessageSource() {
            @Override
            public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
                return defaultMessage == null ? code : defaultMessage;
            }

            @Override
            public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
                return code;
            }

            @Override
            public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
                String defaultMessage = resolvable == null ? null : resolvable.getDefaultMessage();
                return defaultMessage == null ? "" : defaultMessage;
            }
        };
        I18nService i18nService = new I18nService(messageSource);
        DynamicApiRequestExtractor extractor = new DynamicApiRequestExtractor(objectMapper, commonConstants);
        RateLimitAdapter rateLimitAdapter = new AllowAllRateLimitAdapter(commonConstants);

        DynamicDispatcherController controller = new DynamicDispatcherController(
                registry,
                executor,
                extractor,
                properties,
                constants,
                rateLimitAdapter,
                i18nService,
                commonConstants,
                null,
                objectMapper
        );

        try {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/ext/large");
            DeferredResult<ResponseEntity<DynamicApiResponse<Object>>> deferred = controller.dispatch(request);
            ResponseEntity<DynamicApiResponse<Object>> response = awaitResult(deferred);
            assertNotNull(response);
            DynamicApiResponse<Object> body = response.getBody();
            assertNotNull(body);
            assertEquals(constants.getController().getBadRequestCode(), body.getCode());
            assertEquals(constants.getMessage().getResponseTooLarge(), body.getMessage());
        } finally {
            taskExecutor.shutdown();
            cleanupExecutor.shutdown();
            cleanupScheduler.shutdownNow();
        }
    }

    private static class AllowAllRateLimitAdapter extends RateLimitAdapter {
        private AllowAllRateLimitAdapter(CommonConstants commonConstants) {
            super(null, null, commonConstants, null);
        }

        @Override
        public RateLimitDecision tryAcquire(javax.servlet.http.HttpServletRequest request, String policyId) {
            return RateLimitDecision.allow();
        }
    }

    private static class LargeResultStrategy implements ExecuteStrategy {
        @Override
        public String type() {
            return "TEST";
        }

        @Override
        public DynamicApiExecuteResult execute(DynamicApiExecutionContext context) {
            return DynamicApiExecuteResult.success("0123456789-0123456789");
        }
    }
}
