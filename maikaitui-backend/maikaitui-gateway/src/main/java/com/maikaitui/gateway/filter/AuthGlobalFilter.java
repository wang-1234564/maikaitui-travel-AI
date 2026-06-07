package com.maikaitui.gateway.filter;

import com.maikaitui.common.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final JwtTokenProvider jwtTokenProvider;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 无需认证的路径
     */
    private static final String[] EXCLUDE_PATHS = {
            "/api/auth/login",
            "/api/auth/register",
            "/api/file/download/**",
            "/api/ai/**",
            "/api/tourism/miniapp/home",
            "/api/tourism/miniapp/recommend",
            "/api/tourism/attraction/hot",
            "/api/tourism/attraction/list",
            "/api/tourism/region/**",
            "/api/tourism/category/**",
            "/api/tourism/comment/list/**"
    };

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        HttpMethod method = exchange.getRequest().getMethod();

        // AI 路径：有合法 token 则传递用户信息，没有也放行
        if (pathMatcher.match("/api/ai/**", path)) {
            return tryInjectUserInfo(exchange, chain);
        }

        // 其他无需认证的路径直接放行
        if (isExcludedPath(path, method)) {
            log.debug("Public path, skip auth: {} {}", method, path);
            return chain.filter(exchange);
        }

        // 获取 Authorization header
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header: {}", path);
            return writeUnauthorizedResponse(exchange);
        }

        String token = authHeader.substring(7);

        // 验证 Token
        if (!jwtTokenProvider.validateToken(token)) {
            log.warn("Token validation failed: {}", path);
            return writeUnauthorizedResponse(exchange);
        }

        try {
            // 解析用户信息
            Long userId = jwtTokenProvider.getUserId(token);
            String username = jwtTokenProvider.getUsername(token);

            // 将用户信息添加到请求头，传递给下游服务
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header("X-User-Id", String.valueOf(userId))
                    .header("X-Username", username)
                    .build();

            ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();
            return chain.filter(mutatedExchange);
        } catch (Exception e) {
            log.error("Failed to parse token: {}", e.getMessage(), e);
            return writeUnauthorizedResponse(exchange);
        }
    }

    /**
     * 尝试从 token 中提取用户信息注入请求头，失败也不拦截
     */
    private Mono<Void> tryInjectUserInfo(ServerWebExchange exchange, GatewayFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }
        String token = authHeader.substring(7);
        if (!jwtTokenProvider.validateToken(token)) {
            return chain.filter(exchange);
        }
        try {
            Long userId = jwtTokenProvider.getUserId(token);
            String username = jwtTokenProvider.getUsername(token);
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header("X-User-Id", String.valueOf(userId))
                    .header("X-Username", username)
                    .build();
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        } catch (Exception e) {
            log.warn("Failed to parse token for AI path: {}", e.getMessage());
            return chain.filter(exchange);
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }

    /**
     * 判断是否为无需认证的路径
     * /api/tourism/attraction/list 仅 GET 请求跳过认证
     */
    private boolean isExcludedPath(String path, HttpMethod method) {
        if (method != HttpMethod.GET) {
            for (String pattern : EXCLUDE_PATHS) {
                if (pathMatcher.match(pattern, path)) {
                    return pathMatcher.match("/api/auth/**", path)
                            || pathMatcher.match("/api/ai/**", path);
                }
            }
            return false;
        }
        for (String pattern : EXCLUDE_PATHS) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        // GET 景点详情、推荐列表（游客可浏览）
        if (pathMatcher.match("/api/tourism/attraction/*", path)
                && !path.endsWith("/list") && !path.endsWith("/hot")) {
            return true;
        }
        return false;
    }

    /**
     * 返回 401 JSON 错误响应
     */
    private Mono<Void> writeUnauthorizedResponse(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = "{\"code\":401,\"message\":\"Unauthorized\",\"data\":null}";
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}
