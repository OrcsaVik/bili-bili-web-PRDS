/*
 * MIT License
 *
 * Copyright (c) [2025] [OrcasVik]
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *<link>https://github.com/OrcsaVik</link>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */


package com.github.bilibili.gateway;

import com.github.bilibili.framework.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Component
@ConditionalOnClass(MarkAutoConfig.Mark.class)
@AllArgsConstructor
public class JwtAuthenticationGlobalFilter implements GlobalFilter, Ordered {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String AUTH_HEADER = HttpHeaders.AUTHORIZATION;
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String USER_ID_HEADER = "X-User-Id";

    private static final List<String> WHITELIST_PATHS = Arrays.asList("/api/user/login",
            "/api/user/register",
            "/api/user/refresh",
            "/public/",
            "/swagger-ui",
            "/v3/api-docs",
            "/actuator/health",
            "/api/user/test",
            "/api/user/test2",
            "/api/user/login"

    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (isWhitelisted(path)) {
            return chain.filter(exchange);
        }

        String token = extractToken(exchange.getRequest());
        // judge is on the black no matter is refresh or access
        if (isBlacklisted(token)) {
            return unauthorized(exchange.getResponse(), "Token is blacklisted");
        }
        String deviceId = exchange.getRequest().getHeaders().getFirst("X-Device-Id");
        String userAgent = exchange.getRequest().getHeaders().getFirst("User-Agent");

        if (!StringUtils.hasText(token)) {
            return unauthorized(exchange.getResponse(), "Missing authorization token");
        }
        // check if exist change for device or credit
        String currentFingerprint = JwtUtil.calculateFingerprint(deviceId, userAgent);

        try {
            // this is for acc
            Claims claims = JwtUtil.validateToken(token, currentFingerprint, "acc");

            // set the token add the single token for userId
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header(USER_ID_HEADER, claims.get("userId", String.class))
                    // Optional: remove or keep full claims – usually userId is enough downstream
                    // .header("X-User-Claims", claims.toString())
                    .build();

            // TODO add the bannerTip for someGuys
            // by the user key is {banner:$userId}

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (IllegalArgumentException e) {
            return unauthorized(exchange.getResponse(), "Invalid token: " + e.getMessage());
        } catch (Exception e) {
            return unauthorized(exchange.getResponse(), "Authentication failed");
        }
    }

    private String extractToken(ServerHttpRequest request) {
        List<String> headers = request.getHeaders().get(AUTH_HEADER);
        if (headers == null || headers.isEmpty()) {
            return null;
        }
        String header = headers.get(0);
        return StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)
                ? header.substring(BEARER_PREFIX.length())
                : null;
    }

    private boolean isWhitelisted(String path) {
        return WHITELIST_PATHS.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> unauthorized(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().set(HttpHeaders.CONTENT_TYPE, "application/json");

        String body = String.format("{\"code\":401,\"message\":\"%s\"}", message);
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100; // Run early, before routing
    }

    // Separate prefixes. Logic is cleaner.
    static final String BL_ACCESS = "bl:acc:";
    static final String BL_REFRESH = "bl:ref:";

    // block the black when logout
    public boolean isBlacklisted(String token) {
        // Optimization: We assume Access Tokens are checked 99% of the time.
        // You could optimize purely based on token prefix if you want.
        return Boolean.TRUE.equals(redisTemplate.hasKey(BL_ACCESS + token)) ||
                Boolean.TRUE.equals(redisTemplate.hasKey(BL_REFRESH + token));
    }
}