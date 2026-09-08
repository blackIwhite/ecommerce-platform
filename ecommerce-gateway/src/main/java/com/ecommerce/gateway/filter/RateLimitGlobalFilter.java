package com.ecommerce.gateway.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.domain.Range;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitGlobalFilter implements GlobalFilter, Ordered {

    private final ReactiveStringRedisTemplate redisTemplate;

    private static final String RATE_LIMIT_PREFIX = "rate:limit:";
    private static final long WINDOW_MS = 60_000;
    private static final int DEFAULT_LIMIT = 100;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (path.startsWith("/actuator")) {
            return chain.filter(exchange);
        }

        String clientIp = getClientIp(request);
        String key = RATE_LIMIT_PREFIX + clientIp + ":" + path;

        long now = Instant.now().toEpochMilli();
        long windowStart = now - WINDOW_MS;

        return redisTemplate.opsForZSet().removeRangeByScore(key, Range.closed(0.0, (double) windowStart))
                .then(redisTemplate.opsForZSet().add(key, now + ":" + Math.random(), now))
                .then(redisTemplate.opsForZSet().size(key))
                .flatMap(count -> {
                    if (count != null && count > DEFAULT_LIMIT) {
                        log.warn("Rate limit exceeded for IP: {}, path: {}, count: {}", clientIp, path, count);
                        return rateLimitResponse(exchange);
                    }
                    return chain.filter(exchange);
                });
    }

    @Override
    public int getOrder() {
        return -200;
    }

    private String getClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String remoteAddr = request.getRemoteAddress() != null
                ? request.getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
        return remoteAddr;
    }

    private Mono<Void> rateLimitResponse(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = "{\"code\":429,\"message\":\"请求过于频繁，请稍后再试\",\"data\":null}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}
