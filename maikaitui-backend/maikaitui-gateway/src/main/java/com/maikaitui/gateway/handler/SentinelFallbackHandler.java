package com.maikaitui.gateway.handler;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maikaitui.common.core.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Sentinel限流降级处理
 * 当网关触发限流或熔断时，返回429状态码和友好的错误信息
 */
@Slf4j
@Component
@Primary
public class SentinelFallbackHandler implements BlockRequestHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<ServerResponse> handleRequest(ServerWebExchange exchange, Throwable ex) {
        String errorMsg = "Request rate limited";

        if (ex instanceof FlowException) {
            log.warn("触发流控规则: {}", exchange.getRequest().getURI().getPath());
            errorMsg = "请求过于频繁，请稍后再试";
        } else if (ex instanceof DegradeException) {
            log.warn("触发熔断规则: {}", exchange.getRequest().getURI().getPath());
            errorMsg = "服务暂不可用，请稍后再试";
        } else if (ex instanceof ParamFlowException) {
            log.warn("触发热点参数限流: {}", exchange.getRequest().getURI().getPath());
            errorMsg = "请求过于频繁，请稍后再试";
        }

        Result<Void> result = Result.error(429, errorMsg);
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(result);
            return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(bytes);
        } catch (JsonProcessingException e) {
            log.error("序列化Sentinel降级响应失败", e);
            return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("{\"code\":429,\"message\":\"Request rate limited\"}");
        }
    }
}
