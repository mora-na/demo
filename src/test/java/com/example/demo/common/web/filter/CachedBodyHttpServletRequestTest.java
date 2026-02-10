package com.example.demo.common.web.filter;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CachedBodyHttpServletRequestTest {

    @Test
    void getInputStream_returnsCachedBodyMultipleTimes() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType("application/json");
        request.setContent("{\"name\":\"alice\"}".getBytes(StandardCharsets.UTF_8));

        CachedBodyHttpServletRequest cached = new CachedBodyHttpServletRequest(request);

        byte[] first = cached.getInputStream().readAllBytes();
        byte[] second = cached.getInputStream().readAllBytes();

        assertArrayEquals(first, second);
        assertArrayEquals("{\"name\":\"alice\"}".getBytes(StandardCharsets.UTF_8), first);
    }

    @Test
    void getReader_usesRequestEncodingOrUtf8Fallback() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCharacterEncoding("UTF-8");
        request.setContent("hello".getBytes(StandardCharsets.UTF_8));

        CachedBodyHttpServletRequest cached = new CachedBodyHttpServletRequest(request);
        BufferedReader reader = cached.getReader();

        assertEquals("hello", reader.readLine());
    }
}
