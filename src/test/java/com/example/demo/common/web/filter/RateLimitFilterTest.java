package com.example.demo.common.web.filter;

import com.example.demo.common.cache.CacheProperties;
import com.example.demo.common.cache.CacheTool;
import com.example.demo.common.cache.MemoryCacheStore;
import com.example.demo.common.i18n.I18nService;
import com.example.demo.common.web.CommonExcludePathsProperties;
import com.example.demo.common.web.limit.RateLimitProperties;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterChain;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RateLimitFilterTest {

    @Test
    void exceedingRequestLimitReturns429() throws Exception {
        RateLimitProperties properties = new RateLimitProperties();
        properties.setMaxRequests(1);
        properties.setWindowSeconds(60);
        properties.setEnabled(true);

        CacheProperties.Memory memory = new CacheProperties.Memory();
        memory.setCleanupIntervalSeconds(0);
        memory.setMaximumWeightMb(0);
        CacheTool cacheTool = new CacheTool(new MemoryCacheStore(memory));
        CommonExcludePathsProperties commonExcludePaths = new CommonExcludePathsProperties();
        I18nService i18nService = mock(I18nService.class);
        when(i18nService.getMessage(any(javax.servlet.http.HttpServletRequest.class), anyString()))
                .thenReturn("rate limit exceeded");

        RateLimitFilter filter = new RateLimitFilter(properties, commonExcludePaths, cacheTool, i18nService);
        AtomicInteger chainCount = new AtomicInteger();
        FilterChain chain = (req, res) -> {
            chainCount.incrementAndGet();
            res.setContentType("application/json");
            res.getWriter().write("{\"ok\":true}");
            res.getWriter().flush();
        };

        MockHttpServletRequest first = buildRequest();
        MockHttpServletResponse firstResponse = new MockHttpServletResponse();
        filter.doFilter(first, firstResponse, chain);
        assertEquals(1, chainCount.get());
        assertEquals(200, firstResponse.getStatus());

        MockHttpServletResponse secondResponse = new MockHttpServletResponse();
        filter.doFilter(buildRequest(), secondResponse, chain);
        assertEquals(429, secondResponse.getStatus());
        assertEquals(1, chainCount.get(), "rate-limited request should not invoke downstream chain");
    }

    private MockHttpServletRequest buildRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/api/resource");
        request.setRemoteAddr("127.0.0.1");
        return request;
    }

}
