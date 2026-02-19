package com.example.demo.common.web.filter;

import lombok.Getter;
import org.springframework.util.StreamUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 请求体缓存包装器，允许多次读取请求体内容。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Getter
public class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

    /**
     * -- GETTER --
     * 获取缓存的请求体字节数组。
     *
     * @return 请求体字节数组
     *
     */
    private final byte[] cachedBody;
    private final boolean bodyTooLarge;

    /**
     * 构造函数，读取并缓存请求体。
     *
     * @param request 原始请求
     * @throws IOException 读取请求体失败
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
        this.cachedBody = StreamUtils.copyToByteArray(request.getInputStream());
        this.bodyTooLarge = false;
    }

    /**
     * 构造函数，读取并缓存请求体（可选最大字节限制）。
     *
     * @param request      原始请求
     * @param maxBodyBytes 最大缓存字节数（<=0 表示不限制）
     * @throws IOException 读取请求体失败
     */
    public CachedBodyHttpServletRequest(HttpServletRequest request, int maxBodyBytes) throws IOException {
        super(request);
        if (maxBodyBytes <= 0) {
            this.cachedBody = StreamUtils.copyToByteArray(request.getInputStream());
            this.bodyTooLarge = false;
        } else {
            BodyReadResult result = readWithLimit(request, maxBodyBytes);
            this.cachedBody = result.bytes;
            this.bodyTooLarge = result.tooLarge;
        }
    }

    /**
     * 返回可重复读取的输入流。
     *
     * @return ServletInputStream
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @Override
    public ServletInputStream getInputStream() {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(cachedBody);
        return new ServletInputStream() {
            @Override
            public int read() {
                return inputStream.read();
            }

            @Override
            public boolean isFinished() {
                return inputStream.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                // 无异步读取需求，保持空实现
            }
        };
    }

    /**
     * 返回基于缓存的字符读取器。
     *
     * @return BufferedReader
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @Override
    public BufferedReader getReader() {
        Charset charset = resolveCharset();
        return new BufferedReader(new InputStreamReader(getInputStream(), charset));
    }

    /**
     * 解析请求字符集，失败则回退到 UTF-8。
     *
     * @return 字符集
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private Charset resolveCharset() {
        String encoding = getCharacterEncoding();
        if (encoding == null) {
            return StandardCharsets.UTF_8;
        }
        try {
            return Charset.forName(encoding);
        } catch (Exception ex) {
            return StandardCharsets.UTF_8;
        }
    }

    private BodyReadResult readWithLimit(HttpServletRequest request, int maxBodyBytes) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(Math.min(maxBodyBytes, 1024));
        byte[] chunk = new byte[4096];
        int total = 0;
        boolean tooLarge = false;
        int read;
        ServletInputStream inputStream = request.getInputStream();
        while ((read = inputStream.read(chunk)) != -1) {
            int remaining = maxBodyBytes - total;
            if (remaining <= 0) {
                tooLarge = true;
                break;
            }
            int toWrite = Math.min(read, remaining);
            buffer.write(chunk, 0, toWrite);
            total += toWrite;
            if (toWrite < read) {
                tooLarge = true;
                break;
            }
        }
        return new BodyReadResult(buffer.toByteArray(), tooLarge);
    }

    private static final class BodyReadResult {
        private final byte[] bytes;
        private final boolean tooLarge;

        private BodyReadResult(byte[] bytes, boolean tooLarge) {
            this.bytes = bytes;
            this.tooLarge = tooLarge;
        }
    }
}
