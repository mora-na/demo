export default {
    async fetch(request, env) {
        const requestStartedAt = Date.now();
        const incomingRequestId = request.headers.get("X-Request-Id");
        const requestId = incomingRequestId || crypto.randomUUID();
        const cfRay = request.headers.get("cf-ray") || "";

        // ===== 1. 配置 =====
        const WORKER_ORIGIN = (env.WORKER_ORIGIN || "https://demo.morana.dns.army")
            .trim()
            .replace(/\/+$/, "");

        const BACKEND_ORIGIN = (env.BACKEND_ORIGIN || "https://mora.local")
            .trim()
            .replace(/\/+$/, "");

        const STRIP_PREFIX = (env.STRIP_PREFIX || "/prod-api").trim();

        const incomingUrl = new URL(request.url);
        let path = incomingUrl.pathname;

        console.info("worker.request.start", {
            requestId,
            cfRay,
            method: request.method,
            url: incomingUrl.toString(),
            colo: request.cf?.colo || null,
        });

        // ===== 2. 路径重写 =====
        if (STRIP_PREFIX && path.startsWith(STRIP_PREFIX)) {
            path = path.slice(STRIP_PREFIX.length) || "/";
        }

        const targetUrl = new URL(path + incomingUrl.search, BACKEND_ORIGIN);

        // ===== 3. CORS 校验（核心）=====
        const requestOrigin = request.headers.get("Origin");

        // ❗只允许浏览器 + 指定域名
        if (!requestOrigin) {
            console.warn("worker.request.reject", {
                requestId,
                cfRay,
                method: request.method,
                url: incomingUrl.toString(),
                reason: "missing_origin",
                durationMs: Date.now() - requestStartedAt,
            });
            return new Response("Forbidden: missing Origin", {
                status: 403,
                headers: {
                    "Content-Type": "text/plain; charset=utf-8",
                    "X-Request-Id": requestId,
                    "X-Cf-Ray": cfRay,
                },
            });
        }

        const normalizedOrigin = requestOrigin.replace(/\/+$/, "");
        const allowOrigin =
            normalizedOrigin === WORKER_ORIGIN ? requestOrigin : "";

        // ===== 4. 预检请求 =====
        if (request.method === "OPTIONS") {
            if (!allowOrigin) {
                console.warn("worker.request.reject", {
                    requestId,
                    cfRay,
                    method: request.method,
                    url: incomingUrl.toString(),
                    reason: "origin_not_allowed",
                    origin: requestOrigin,
                    durationMs: Date.now() - requestStartedAt,
                });
                return new Response(
                    JSON.stringify({
                        message: "CORS origin not allowed",
                        origin: requestOrigin,
                    }),
                    {
                        status: 403,
                        headers: {
                            "Content-Type": "application/json; charset=utf-8",
                            "X-Request-Id": requestId,
                            "X-Cf-Ray": cfRay,
                            "Vary": "Origin",
                        },
                    }
                );
            }

            const allowHeaders =
                request.headers.get("Access-Control-Request-Headers") ||
                "Content-Type,Authorization";

            return new Response(null, {
                status: 204,
                headers: {
                    "Access-Control-Allow-Origin": allowOrigin,
                    "Access-Control-Allow-Credentials": "true",
                    "Access-Control-Allow-Methods": "GET,POST,PUT,PATCH,DELETE,OPTIONS",
                    "Access-Control-Allow-Headers": allowHeaders,
                    "Access-Control-Max-Age": "86400",
                    "X-Request-Id": requestId,
                    "X-Cf-Ray": cfRay,
                    "Vary": "Origin",
                },
            });
        }

        // ===== 5. 非法来源直接拒绝 =====
        if (!allowOrigin) {
            console.warn("worker.request.reject", {
                requestId,
                cfRay,
                method: request.method,
                url: incomingUrl.toString(),
                reason: "origin_not_allowed",
                origin: requestOrigin,
                durationMs: Date.now() - requestStartedAt,
            });
            return new Response(
                JSON.stringify({
                    message: "Forbidden: origin not allowed",
                    origin: requestOrigin,
                }),
                {
                    status: 403,
                    headers: {
                        "Content-Type": "application/json; charset=utf-8",
                        "X-Request-Id": requestId,
                        "X-Cf-Ray": cfRay,
                        "Vary": "Origin",
                    },
                }
            );
        }

        // ===== 6. 构造转发请求头 =====
        const headers = new Headers(request.headers);

        // ✅ 保持真实 Origin（关键！）
        headers.set("Origin", requestOrigin);
        headers.set("X-Request-Id", requestId);
        headers.set("X-Trace-Id", requestId);
        if (cfRay) {
            headers.set("X-Cf-Ray", cfRay);
        }

        // 透传真实来源（可用于后端审计 / 风控）
        headers.set("X-Real-Origin", requestOrigin);

        // 代理链路信息
        headers.set("X-Forwarded-Host", incomingUrl.host);
        headers.set("X-Forwarded-Proto", incomingUrl.protocol.replace(":", ""));

        const clientIp = request.headers.get("CF-Connecting-IP");
        if (clientIp) {
            headers.set("X-Forwarded-For", clientIp);
        }

        // 可选：标识来自 Worker（后端可做白名单）
        headers.set("X-From-Worker", "true");

        // 删除不安全或无意义头
        headers.delete("Host");
        // ❗不要删 Referer（有些后端会用）
        // headers.delete("Referer");

        // ===== 7. 构造请求 =====
        const init = {
            method: request.method,
            headers,
            redirect: "manual",
        };

        if (request.method !== "GET" && request.method !== "HEAD") {
            init.body = request.body;
        }

        try {
            // ===== 8. 转发请求 =====
            const upstreamStartedAt = Date.now();
            console.info("worker.origin.fetch.start", {
                requestId,
                cfRay,
                method: request.method,
                targetUrl: targetUrl.toString(),
            });
            const response = await fetch(targetUrl.toString(), init);

            // ===== 9. 构造响应 =====
            const respHeaders = new Headers(response.headers);
            const durationMs = Date.now() - requestStartedAt;

            // ❗强制覆盖 CORS（不要信任后端返回）
            respHeaders.set("Access-Control-Allow-Origin", allowOrigin);
            respHeaders.set("Access-Control-Allow-Credentials", "true");

            // 防缓存污染（非常重要）
            respHeaders.set("Vary", "Origin");

            // 可选安全头（推荐）
            respHeaders.set("X-Content-Type-Options", "nosniff");
            respHeaders.set("X-Frame-Options", "SAMEORIGIN");
            respHeaders.set("Referrer-Policy", "strict-origin-when-cross-origin");
            respHeaders.set("X-Request-Id", requestId);
            if (cfRay) {
                respHeaders.set("X-Cf-Ray", cfRay);
            }

            console.info("worker.origin.fetch.complete", {
                requestId,
                cfRay,
                method: request.method,
                targetUrl: targetUrl.toString(),
                status: response.status,
                upstreamDurationMs: Date.now() - upstreamStartedAt,
                durationMs,
            });

            return new Response(response.body, {
                status: response.status,
                statusText: response.statusText,
                headers: respHeaders,
            });
        } catch (err) {
            // ===== 10. 异常处理 =====
            console.error("worker.origin.fetch.error", {
                requestId,
                cfRay,
                method: request.method,
                targetUrl: targetUrl.toString(),
                durationMs: Date.now() - requestStartedAt,
                error: err?.message || String(err),
            });
            return new Response(
                JSON.stringify({
                    message: "Bad Gateway",
                    requestId,
                    cfRay,
                    error: err?.message || String(err),
                }),
                {
                    status: 502,
                    headers: {
                        "Content-Type": "application/json; charset=utf-8",
                        "Access-Control-Allow-Origin": allowOrigin,
                        "Access-Control-Allow-Credentials": "true",
                        "X-Request-Id": requestId,
                        "X-Cf-Ray": cfRay,
                        "Vary": "Origin",
                    },
                }
            );
        }
    },
};
