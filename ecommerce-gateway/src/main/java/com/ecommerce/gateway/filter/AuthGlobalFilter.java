package com.ecommerce.gateway.filter;

import com.ecommerce.gateway.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final JwtProperties jwtProperties;
    private final ReactiveStringRedisTemplate redisTemplate;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final String TOKEN_BLACKLIST_PREFIX = "auth:blacklist:";

    private static final List<String> WHITE_LIST = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/refresh",
            "/api/auth/sms/**",
            "/api/auth/admin/login",
            "/api/product/spu/**",
            "/api/product/sku/**",
            "/api/product/category/tree",
            "/api/product/brand/list",
            "/api/product/article/**",
            "/api/marketing/template/available",
            "/api/marketing/promotion/active",
            "/api/file/internal/**",
            "/api/file/download/**"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (isWhiteListed(path)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header for path: {}", path);
            return unauthorizedResponse(exchange, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = validateToken(token);
            Long userId = claims.get("userId", Long.class);
            String phone = claims.get("phone", String.class);

            if (userId == null) {
                return unauthorizedResponse(exchange, "Invalid token: missing userId");
            }

            return redisTemplate.hasKey(TOKEN_BLACKLIST_PREFIX + token)
                    .flatMap(isBlacklisted -> {
                        if (Boolean.TRUE.equals(isBlacklisted)) {
                            log.warn("Token is blacklisted for path: {}", path);
                            return unauthorizedResponse(exchange, "Token has been invalidated");
                        }

                        ServerHttpRequest.Builder requestBuilder = request.mutate()
                                .header("X-User-Id", String.valueOf(userId));

                        if (phone != null && !phone.isEmpty()) {
                            requestBuilder.header("X-User-Phone", phone);
                        }

                        ServerHttpRequest mutatedRequest = requestBuilder.build();
                        return chain.filter(exchange.mutate().request(mutatedRequest).build());
                    });

        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired for path: {}", path);
            return unauthorizedResponse(exchange, "Token expired");
        } catch (MalformedJwtException | UnsupportedJwtException | SecurityException e) {
            log.warn("Invalid JWT token for path: {}, error: {}", path, e.getMessage());
            return unauthorizedResponse(exchange, "Invalid token");
        } catch (Exception e) {
            log.error("JWT validation error for path: {}", path, e);
            return unauthorizedResponse(exchange, "Token validation failed");
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private boolean isWhiteListed(String path) {
        return WHITE_LIST.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private Claims validateToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = "{\"code\":401,\"message\":\"" + message + "\",\"data\":null}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}
