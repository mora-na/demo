# Microservice Transition (Phase 1)

This repository now contains a non-breaking transition skeleton:

- `app`: existing modular monolith (kept as-is)
- `gateway`: external entry for gradual traffic switching
- `auth-service`: auth-domain service wrapper
- `order-service`: order-domain service wrapper
- `system-api`: cross-domain API contracts used by `auth`/`order`

## Goal

Use the Strangler pattern to move traffic from monolith to microservices incrementally, without a one-shot rewrite.

## Modules

- `gateway` on port `8088` by default
- `auth-service` on port `9001` by default
- `order-service` on port `9002` by default
- `app` (monolith) on port `8080` by default
- `nacos` expected at `127.0.0.1:8848` by default

## Gateway routing strategy

`gateway/src/main/resources/application.yml` uses:

- Canary route for `/auth/**` only when header `X-Canary: auth-service` exists, forwarding to `lb://auth-service`.
- Canary route for `/orders/**` only when header `X-Canary: order-service` exists, forwarding to `lb://order-service`.
- Fallback route sends all other traffic to monolith.

This keeps production behavior stable by default while enabling targeted validation.

## Local startup order

1. Start infra dependencies (PostgreSQL and Redis) if needed.
2. Start Nacos (`NACOS_SERVER_ADDR` if not `127.0.0.1:8848`).
3. Start `app` (current stable path).
4. Start `auth-service` and `order-service` (they register to Nacos).
5. Start `gateway`.

## Quick verification

Route to monolith (default):

```bash
curl -i http://localhost:8088/auth/captcha
```

Route to auth microservice canary:

```bash
curl -i -H "X-Canary: auth-service" http://localhost:8088/auth/captcha
```

Route to order microservice canary:

```bash
curl -i -H "X-Canary: order-service" http://localhost:8088/orders
```

## Next split recommendations

1. Extract service contracts (DTO + facade interfaces) into dedicated API modules.
2. Replace direct mapper/table coupling across domains with API calls or async events.
3. Introduce service registry/config center (e.g. Nacos) and replace static URLs.
4. Move auth/token validation to gateway + per-service authorization policies.
5. Add distributed tracing and unified log aggregation before broad traffic cutover.
