package com.example.demo.common.web.filter;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CachedBodyHttpServletRequestTest {

    private static byte[] readAllBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, read);
        }
        return outputStream.toByteArray();
    }

    @Test
    void getInputStream_returnsCachedBodyMultipleTimes() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType("application/json");
        request.setContent("{\"name\":\"alice\"}".getBytes(StandardCharsets.UTF_8));

        CachedBodyHttpServletRequest cached = new CachedBodyHttpServletRequest(request);

        byte[] first = readAllBytes(cached.getInputStream());
        byte[] second = readAllBytes(cached.getInputStream());

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
