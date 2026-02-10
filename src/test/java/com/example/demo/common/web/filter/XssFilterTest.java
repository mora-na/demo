package com.example.demo.common.web.filter;

import com.example.demo.common.web.CommonExcludePathsProperties;
import com.example.demo.common.web.xss.XssHttpServletRequestWrapper;
import com.example.demo.common.web.xss.XssProperties;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

class XssFilterTest {

    @Test
    void disabledFilter_passesThroughOriginalRequest() throws Exception {
        XssProperties properties = new XssProperties();
        properties.setEnabled(false);
        CommonExcludePathsProperties commonExcludePaths = new CommonExcludePathsProperties();
        XssFilter filter = new XssFilter(properties, commonExcludePaths);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<ServletRequest> captured = new AtomicReference<>();
        FilterChain chain = (req, res) -> captured.set(req);

        filter.doFilter(request, response, chain);

        assertSame(request, captured.get());
    }

    @Test
    void enabledFilter_wrapsRequestWhenNotExcluded() throws Exception {
        XssProperties properties = new XssProperties();
        properties.setEnabled(true);
        CommonExcludePathsProperties commonExcludePaths = new CommonExcludePathsProperties();
        XssFilter filter = new XssFilter(properties, commonExcludePaths);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<ServletRequest> captured = new AtomicReference<>();
        FilterChain chain = (req, res) -> captured.set(req);

        filter.doFilter(request, response, chain);

        assertInstanceOf(XssHttpServletRequestWrapper.class, captured.get());
    }

    @Test
    void excludedPath_passesThroughOriginalRequest() throws Exception {
        XssProperties properties = new XssProperties();
        properties.setEnabled(true);
        properties.setExcludePaths(Collections.singletonList("/public/**"));
        CommonExcludePathsProperties commonExcludePaths = new CommonExcludePathsProperties();
        XssFilter filter = new XssFilter(properties, commonExcludePaths);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/public/info");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<ServletRequest> captured = new AtomicReference<>();
        FilterChain chain = (req, res) -> captured.set(req);

        filter.doFilter(request, response, chain);

        assertSame(request, captured.get());
    }
}
