package com.example.demo.extension.executor;

import com.example.demo.extension.api.executor.DynamicApiExecuteResult;
import com.example.demo.extension.api.executor.DynamicApiExecutionContext;
import com.example.demo.extension.api.executor.DynamicApiTerminationReason;
import com.example.demo.extension.api.executor.ExecuteStrategy;
import com.example.demo.extension.api.request.DynamicApiRequest;
import com.example.demo.extension.config.DynamicApiConstants;
import com.example.demo.extension.model.DynamicApiTypeCodes;
import com.example.demo.extension.model.HttpForwardConfig;
import com.example.demo.extension.support.DynamicApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.execchain.RequestAbortedException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.MDC;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PreDestroy;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * HTTP 转发策略。
 */
@Slf4j
@Component
public class HttpForwardStrategy implements ExecuteStrategy {

    private final DynamicApiConstants constants;
    private final CloseableHttpClient httpClient;
    private final ConcurrentHashMap<DynamicApiExecutionContext, HttpRequestBase> running = new ConcurrentHashMap<>();

    public HttpForwardStrategy(DynamicApiConstants constants) {
        this.constants = constants;
        DynamicApiConstants.Http http = constants.getHttp();
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(Math.max(1, http.getMaxTotalConnections()));
        connectionManager.setDefaultMaxPerRoute(Math.max(1, http.getMaxConnectionsPerRoute()));
        this.httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .disableAutomaticRetries()
                .evictExpiredConnections()
                .evictIdleConnections(Math.max(1, http.getIdleEvictSeconds()), TimeUnit.SECONDS)
                .build();
    }

    @Override
    public String type() {
        return DynamicApiTypeCodes.HTTP;
    }

    @Override
    public String displayName() {
        return "HTTP";
    }

    @Override
    public Object parseConfig(String configJson, ObjectMapper objectMapper) throws Exception {
        if (StringUtils.isBlank(configJson)) {
            throw new DynamicApiException(constants.getController().getBadRequestCode(),
                    constants.getMessage().getConfigInvalid());
        }
        HttpForwardConfig config = objectMapper.readValue(configJson, HttpForwardConfig.class);
        if (config == null || StringUtils.isBlank(config.getUrl())) {
            throw new DynamicApiException(constants.getController().getBadRequestCode(),
                    constants.getMessage().getHttpInvalid());
        }
        return config;
    }

    @Override
    public DynamicApiExecuteResult execute(DynamicApiExecutionContext context) {
        if (Thread.currentThread().isInterrupted()) {
            return DynamicApiExecuteResult.error(constants.getController().getServiceUnavailableCode(),
                    constants.getMessage().getTimeout(),
                    DynamicApiTerminationReason.TIMEOUT);
        }
        Object configObj = context.getConfig();
        if (!(configObj instanceof HttpForwardConfig)) {
            return DynamicApiExecuteResult.error(constants.getController().getBadRequestCode(),
                    constants.getMessage().getHttpInvalid(),
                    DynamicApiTerminationReason.ERROR);
        }
        HttpForwardConfig config = (HttpForwardConfig) configObj;
        if (StringUtils.isBlank(config.getUrl())) {
            return DynamicApiExecuteResult.error(constants.getController().getBadRequestCode(),
                    constants.getMessage().getHttpInvalid(),
                    DynamicApiTerminationReason.ERROR);
        }
        DynamicApiRequest apiRequest = context.getRequest();
        String method = resolveMethod(config, apiRequest);
        HttpMethod httpMethod = HttpMethod.resolve(method);
        if (httpMethod == null) {
            return DynamicApiExecuteResult.error(constants.getController().getBadRequestCode(),
                    constants.getMessage().getMethodInvalid(),
                    DynamicApiTerminationReason.ERROR);
        }
        String targetUrl = resolveUrl(config, apiRequest, context.getRequest().getPathVariables());
        if (!validateTargetUrl(targetUrl)) {
            return DynamicApiExecuteResult.error(constants.getController().getBadRequestCode(),
                    constants.getMessage().getHttpInvalid(),
                    DynamicApiTerminationReason.ERROR);
        }
        HttpHeaders headers = buildHeaders(config, apiRequest);
        String body = apiRequest == null ? null : apiRequest.getRawBody();
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = createRestTemplate(context, (int) context.getTimeoutMs());
        try {
            String response = executeWithLimit(restTemplate, targetUrl, httpMethod, entity);
            return DynamicApiExecuteResult.success(response);
        } catch (DynamicApiException ex) {
            return DynamicApiExecuteResult.error(ex.getCode(), ex.getMessageKey(), DynamicApiTerminationReason.ERROR);
        } catch (Exception ex) {
            if (Thread.currentThread().isInterrupted()
                    || ex instanceof InterruptedIOException
                    || ex instanceof RequestAbortedException) {
                log.debug("Dynamic api http execute interrupted: apiId={}, path={}, method={}, targetUrl={}",
                        context.getApiId(),
                        context.getPath(),
                        context.getMethod(),
                        targetUrl);
                return DynamicApiExecuteResult.error(constants.getController().getServiceUnavailableCode(),
                        constants.getMessage().getTimeout(),
                        DynamicApiTerminationReason.TIMEOUT);
            }
            String traceId = MDC.get("traceId");
            log.error("Dynamic api http execute failed: apiId={}, path={}, method={}, targetUrl={}, traceId={}",
                    context.getApiId(),
                    context.getPath(),
                    context.getMethod(),
                    targetUrl,
                    traceId,
                    ex);
            return DynamicApiExecuteResult.error(constants.getController().getInternalServerErrorCode(),
                    constants.getMessage().getExecuteFailed(),
                    DynamicApiTerminationReason.ERROR);
        } finally {
            running.remove(context);
        }
    }

    private String resolveMethod(HttpForwardConfig config, DynamicApiRequest request) {
        if (StringUtils.isNotBlank(config.getMethod())) {
            return config.getMethod().trim().toUpperCase(Locale.ROOT);
        }
        return request == null ? "GET" : request.getMethod();
    }

    private String resolveUrl(HttpForwardConfig config, DynamicApiRequest request, java.util.Map<String, String> pathVariables) {
        String url = config.getUrl().trim();
        String expanded = UriComponentsBuilder.fromUriString(url)
                .buildAndExpand(pathVariables == null ? java.util.Collections.emptyMap() : pathVariables)
                .toUriString();
        if (config.isPassQuery() && request != null && request.getQueryParams() != null) {
            Map<String, List<String>> queryParams = request.getQueryParams();
            if (!queryParams.isEmpty()) {
                UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(expanded);
                for (Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
                    String key = entry.getKey();
                    List<String> values = entry.getValue();
                    if (values == null || values.isEmpty()) {
                        builder.queryParam(key);
                    } else {
                        for (String value : values) {
                            builder.queryParam(key, value);
                        }
                    }
                }
                return builder.toUriString();
            }
        }
        return expanded;
    }

    private boolean validateTargetUrl(String targetUrl) {
        if (StringUtils.isBlank(targetUrl)) {
            return false;
        }
        URI uri;
        try {
            uri = URI.create(targetUrl);
        } catch (Exception ex) {
            return false;
        }
        String scheme = uri.getScheme();
        if (StringUtils.isBlank(scheme)) {
            return false;
        }
        List<String> allowedSchemes = constants.getHttp().getAllowedSchemes();
        if (allowedSchemes != null && !allowedSchemes.isEmpty()) {
            boolean match = false;
            for (String allowed : allowedSchemes) {
                if (allowed != null && scheme.equalsIgnoreCase(allowed.trim())) {
                    match = true;
                    break;
                }
            }
            if (!match) {
                return false;
            }
        }
        String host = uri.getHost();
        if (StringUtils.isBlank(host)) {
            return false;
        }
        if (isHostBlocked(host, constants.getHttp().getBlockedHosts())) {
            return false;
        }
        List<String> allowedHosts = constants.getHttp().getAllowedHosts();
        if (allowedHosts != null && !allowedHosts.isEmpty() && !isHostAllowed(host, allowedHosts)) {
            return false;
        }
        if (!validateHostByCidr(host)) {
            return false;
        }
        if (constants.getHttp().isBlockPrivateNetwork() && isPrivateAddress(host)) {
            return false;
        }
        return true;
    }

    private boolean isHostAllowed(String host, List<String> allowedHosts) {
        for (String pattern : allowedHosts) {
            if (matchHostPattern(host, pattern)) {
                return true;
            }
        }
        return false;
    }

    private boolean isHostBlocked(String host, List<String> blockedHosts) {
        if (blockedHosts == null || blockedHosts.isEmpty()) {
            return false;
        }
        for (String pattern : blockedHosts) {
            if (matchHostPattern(host, pattern)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchHostPattern(String host, String pattern) {
        if (StringUtils.isBlank(pattern)) {
            return false;
        }
        String normalized = pattern.trim().toLowerCase(Locale.ROOT);
        String target = host.toLowerCase(Locale.ROOT);
        if (normalized.equals(target)) {
            return true;
        }
        if (normalized.contains("*")) {
            String regex = "^" + wildcardToRegex(normalized) + "$";
            return target.matches(regex);
        }
        return false;
    }

    private String wildcardToRegex(String pattern) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < pattern.length(); i++) {
            char ch = pattern.charAt(i);
            if (ch == '*') {
                builder.append(".*");
                continue;
            }
            if ("\\.[]{}()+-^$|?".indexOf(ch) >= 0) {
                builder.append('\\');
            }
            builder.append(ch);
        }
        return builder.toString();
    }

    private boolean isPrivateAddress(String host) {
        try {
            InetAddress[] addresses = InetAddress.getAllByName(host);
            for (InetAddress address : addresses) {
                if (address.isAnyLocalAddress()
                        || address.isLoopbackAddress()
                        || address.isSiteLocalAddress()
                        || address.isLinkLocalAddress()
                        || address.isMulticastAddress()) {
                    return true;
                }
            }
            return false;
        } catch (UnknownHostException ex) {
            return constants.getHttp().isBlockUnknownHost();
        }
    }

    private boolean validateHostByCidr(String host) {
        List<String> allowedCidrs = constants.getHttp().getAllowedCidrs();
        List<String> blockedCidrs = constants.getHttp().getBlockedCidrs();
        boolean hasAllowed = allowedCidrs != null && !allowedCidrs.isEmpty();
        boolean hasBlocked = blockedCidrs != null && !blockedCidrs.isEmpty();
        if (!hasAllowed && !hasBlocked) {
            return true;
        }
        InetAddress[] addresses;
        try {
            addresses = InetAddress.getAllByName(host);
        } catch (UnknownHostException ex) {
            if (constants.getHttp().isBlockUnknownHost()) {
                return false;
            }
            return !hasAllowed;
        }
        if (hasBlocked) {
            for (InetAddress address : addresses) {
                if (matchAnyCidr(address, blockedCidrs)) {
                    return false;
                }
            }
        }
        if (hasAllowed) {
            for (InetAddress address : addresses) {
                if (matchAnyCidr(address, allowedCidrs)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    private boolean matchAnyCidr(InetAddress address, List<String> cidrs) {
        if (address == null || cidrs == null || cidrs.isEmpty()) {
            return false;
        }
        for (String cidr : cidrs) {
            CidrRange range = parseCidr(cidr);
            if (range != null && range.matches(address)) {
                return true;
            }
        }
        return false;
    }

    private CidrRange parseCidr(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        String trimmed = value.trim();
        String[] parts = trimmed.split("/", 2);
        String ipPart = parts[0].trim();
        if (ipPart.isEmpty()) {
            return null;
        }
        try {
            InetAddress base = InetAddress.getByName(ipPart);
            byte[] bytes = base.getAddress();
            int bits = bytes.length * 8;
            int prefix = bits;
            if (parts.length == 2) {
                String prefixText = parts[1].trim();
                if (!prefixText.isEmpty()) {
                    prefix = Integer.parseInt(prefixText);
                }
            }
            if (prefix < 0 || prefix > bits) {
                return null;
            }
            BigInteger baseInt = new BigInteger(1, bytes);
            BigInteger allOnes = BigInteger.ONE.shiftLeft(bits).subtract(BigInteger.ONE);
            BigInteger mask = prefix == 0 ? BigInteger.ZERO : allOnes.shiftLeft(bits - prefix).and(allOnes);
            BigInteger network = baseInt.and(mask);
            return new CidrRange(network, mask, bytes.length);
        } catch (Exception ex) {
            return null;
        }
    }

    private static class CidrRange {
        private final BigInteger network;
        private final BigInteger mask;
        private final int byteLength;

        private CidrRange(BigInteger network, BigInteger mask, int byteLength) {
            this.network = network;
            this.mask = mask;
            this.byteLength = byteLength;
        }

        private boolean matches(InetAddress address) {
            if (address == null) {
                return false;
            }
            byte[] bytes = address.getAddress();
            if (bytes == null || bytes.length != byteLength) {
                return false;
            }
            BigInteger value = new BigInteger(1, bytes);
            return value.and(mask).equals(network);
        }
    }

    private HttpHeaders buildHeaders(HttpForwardConfig config, DynamicApiRequest request) {
        HttpHeaders headers = new HttpHeaders();
        if (request != null && config.isPassHeaders()) {
            Map<String, String> requestHeaders = request.getHeaders();
            for (Map.Entry<String, String> entry : requestHeaders.entrySet()) {
                String name = entry.getKey();
                if (name == null) {
                    continue;
                }
                if ("host".equalsIgnoreCase(name) || "content-length".equalsIgnoreCase(name)) {
                    continue;
                }
                headers.add(name, entry.getValue());
            }
        }
        if (config.getHeaders() != null && !config.getHeaders().isEmpty()) {
            for (java.util.Map.Entry<String, String> entry : config.getHeaders().entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    headers.set(entry.getKey(), entry.getValue());
                }
            }
        }
        if (!headers.containsKey(HttpHeaders.CONTENT_TYPE) && request != null) {
            String contentType = null;
            for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
                if (HttpHeaders.CONTENT_TYPE.equalsIgnoreCase(entry.getKey())) {
                    contentType = entry.getValue();
                    break;
                }
            }
            if (contentType != null) {
                headers.set(HttpHeaders.CONTENT_TYPE, contentType);
            }
        }
        return headers;
    }

    @Override
    public void onTimeout(DynamicApiExecutionContext context) {
        cancelConnection(context);
    }

    @Override
    public void onError(DynamicApiExecutionContext context, Throwable error) {
        cancelConnection(context);
    }

    @Override
    public void onCancel(DynamicApiExecutionContext context, Throwable cause) {
        cancelConnection(context);
    }

    private RestTemplate createRestTemplate(DynamicApiExecutionContext context, int timeoutMs) {
        CancellableClientHttpRequestFactory factory =
                new CancellableClientHttpRequestFactory(httpClient, context, running);
        int timeout = timeoutMs <= 0 ? 3000 : timeoutMs;
        factory.setConnectTimeout(timeout);
        factory.setConnectionRequestTimeout(timeout);
        factory.setReadTimeout(timeout);
        return new RestTemplate(factory);
    }

    private String executeWithLimit(RestTemplate restTemplate,
                                    String targetUrl,
                                    HttpMethod httpMethod,
                                    HttpEntity<String> entity) {
        long maxBytes = constants.getExecute().getMaxResponseBytes();
        return restTemplate.execute(targetUrl, httpMethod,
                restTemplate.httpEntityCallback(entity, String.class),
                response -> readLimitedResponse(response, maxBytes));
    }

    private String readLimitedResponse(ClientHttpResponse response, long maxBytes) throws java.io.IOException {
        if (response == null) {
            return null;
        }
        if (maxBytes > 0) {
            long contentLength = response.getHeaders().getContentLength();
            if (contentLength > maxBytes) {
                throw new DynamicApiException(constants.getController().getBadRequestCode(),
                        constants.getMessage().getResponseTooLarge());
            }
        }
        InputStream body = response.getBody();
        if (body == null) {
            return null;
        }
        byte[] data = readWithLimit(body, maxBytes);
        Charset charset = resolveCharset(response.getHeaders());
        return new String(data, charset);
    }

    private Charset resolveCharset(HttpHeaders headers) {
        if (headers == null || headers.getContentType() == null || headers.getContentType().getCharset() == null) {
            return StandardCharsets.UTF_8;
        }
        return headers.getContentType().getCharset();
    }

    private byte[] readWithLimit(InputStream body, long maxBytes) throws java.io.IOException {
        int bufferSize = 4096;
        byte[] buffer = new byte[bufferSize];
        int read;
        int total = 0;
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        while ((read = body.read(buffer)) >= 0) {
            if (read == 0) {
                continue;
            }
            total += read;
            if (maxBytes > 0 && total > maxBytes) {
                throw new DynamicApiException(constants.getController().getBadRequestCode(),
                        constants.getMessage().getResponseTooLarge());
            }
            out.write(buffer, 0, read);
        }
        return out.toByteArray();
    }

    private void cancelConnection(DynamicApiExecutionContext context) {
        if (context == null) {
            return;
        }
        HttpRequestBase request = running.remove(context);
        if (request != null) {
            try {
                request.abort();
            } catch (Exception ignored) {
                // ignore
            }
        }
    }

    @PreDestroy
    public void closeClient() {
        try {
            httpClient.close();
        } catch (Exception ignored) {
            // ignore
        }
    }

    private static class CancellableClientHttpRequestFactory extends HttpComponentsClientHttpRequestFactory {
        private final DynamicApiExecutionContext context;
        private final ConcurrentHashMap<DynamicApiExecutionContext, HttpRequestBase> running;

        private CancellableClientHttpRequestFactory(CloseableHttpClient httpClient,
                                                    DynamicApiExecutionContext context,
                                                    ConcurrentHashMap<DynamicApiExecutionContext, HttpRequestBase> running) {
            super(httpClient);
            this.context = context;
            this.running = running;
        }

        @Override
        protected @NonNull HttpUriRequest createHttpUriRequest(@NonNull HttpMethod httpMethod, @NonNull URI uri) {
            HttpUriRequest request = super.createHttpUriRequest(httpMethod, uri);
            if (context != null && running != null && request instanceof HttpRequestBase) {
                running.put(context, (HttpRequestBase) request);
            }
            return request;
        }
    }
}
