package com.example.demo.common.web.filter;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterChain;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class TraceIdFilterTest {

    @Test
    void traceIdIsSetAndCleared() throws Exception {
        TraceIdFilter filter = new TraceIdFilter();
        FilterChain chain = (request, response) -> assertNotNull(MDC.get("traceId"));

        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), chain);

        assertNull(MDC.get("traceId"));
    }
}
