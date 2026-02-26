package com.example.demo.auth.support;

import com.example.demo.auth.config.AuthConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * 认证令牌解析器，按优先级从请求头或参数中提取令牌。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Component
public class AuthTokenResolver {

    private final AuthConstants systemConstants;

    public AuthTokenResolver(AuthConstants systemConstants) {
        this.systemConstants = systemConstants;
    }

    /**
     * 解析请求中的访问令牌，仅支持 Authorization 头与 SSE query 参数。
     *
     * @param request HTTP 请求
     * @return 解析到的令牌字符串，未找到返回 null
     */
    public String resolve(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        AuthConstants.Token tokenConstants = systemConstants.getToken();
        String authorization = StringUtils.trimToNull(request.getHeader(tokenConstants.getAuthorizationHeader()));
        if (StringUtils.isBlank(authorization)) {
            return resolveSseToken(request);
        }
        String bearerPrefix = tokenConstants.getBearerPrefix();
        if (StringUtils.isBlank(bearerPrefix)) {
            return authorization.trim();
        }
        if (authorization.regionMatches(true, 0, bearerPrefix, 0, bearerPrefix.length())) {
            String token = authorization.substring(bearerPrefix.length()).trim();
            return StringUtils.isNotBlank(token) ? token : null;
        }
        return resolveSseToken(request);
    }

    private String resolveSseToken(HttpServletRequest request) {
        if (request == null || !isSseRequest(request) || !isNoticeStream(request)) {
            return null;
        }
        String token = StringUtils.trimToNull(request.getParameter("token"));
        return StringUtils.isNotBlank(token) ? token : null;
    }

    private boolean isSseRequest(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return accept != null && accept.contains("text/event-stream");
    }

    private boolean isNoticeStream(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri != null && uri.endsWith("/notices/stream");
    }
}
