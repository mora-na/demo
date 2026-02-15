package com.example.demo.common.web.filter;

import lombok.Getter;
import org.springframework.util.StreamUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
}
