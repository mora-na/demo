package com.example.demo.common.web.filter;

import com.example.demo.common.web.limit.RateLimitProperties;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterChain;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RateLimitFilterTest {

    @Test
    void exceedingRequestLimitReturns429() throws Exception {
        RateLimitProperties properties = new RateLimitProperties();
        properties.setMaxRequests(1);
        properties.setWindowSeconds(60);

        RateLimitFilter filter = new RateLimitFilter(properties);
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
