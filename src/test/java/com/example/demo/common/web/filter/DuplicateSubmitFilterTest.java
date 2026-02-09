package com.example.demo.common.web.filter;

import com.example.demo.common.web.limit.DuplicateSubmitProperties;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterChain;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DuplicateSubmitFilterTest {

    @Test
    void duplicateBodyWithinIntervalIsRejected() throws Exception {
        DuplicateSubmitProperties properties = new DuplicateSubmitProperties();
        properties.setIntervalMillis(1_000);

        DuplicateSubmitFilter filter = new DuplicateSubmitFilter(properties);
        AtomicInteger chainCount = new AtomicInteger();
        FilterChain chain = (req, res) -> {
            chainCount.incrementAndGet();
            res.setContentType("application/json");
            res.getWriter().write("{\"ok\":true}");
            res.getWriter().flush();
        };

        MockHttpServletRequest first = buildRequest("{\"name\":\"alice\"}");
        MockHttpServletResponse firstResponse = new MockHttpServletResponse();
        filter.doFilter(first, firstResponse, chain);
        assertEquals(1, chainCount.get());
        assertEquals(200, firstResponse.getStatus());

        MockHttpServletResponse secondResponse = new MockHttpServletResponse();
        filter.doFilter(buildRequest("{\"name\":\"alice\"}"), secondResponse, chain);
        assertEquals(409, secondResponse.getStatus());
        assertEquals(1, chainCount.get(), "duplicate request should not reach downstream chain");
    }

    private MockHttpServletRequest buildRequest(String body) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/api/echo");
        request.setContentType("application/json");
        request.setContent(body.getBytes(StandardCharsets.UTF_8));
        request.setRemoteAddr("127.0.0.1");
        return request;
    }
}
