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
     * 解析请求中的访问令牌，优先级：Authorization > X-Auth-Token。
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
        if (StringUtils.isNotBlank(authorization)) {
            String bearerPrefix = tokenConstants.getBearerPrefix();
            if (StringUtils.isNotBlank(bearerPrefix)
                    && authorization.regionMatches(true, 0, bearerPrefix, 0, bearerPrefix.length())) {
                String token = authorization.substring(bearerPrefix.length()).trim();
                return StringUtils.isNotBlank(token) ? token : null;
            }
            return authorization.trim();
        }
        String token = StringUtils.trimToNull(request.getHeader(tokenConstants.getFallbackTokenHeader()));
        if (StringUtils.isNotBlank(token)) {
            return token.trim();
        }
        String queryToken = request.getParameter(tokenConstants.getQueryTokenParameter());
        if (StringUtils.isNotBlank(queryToken)) {
            return queryToken;
        }
        return null;
    }
}
