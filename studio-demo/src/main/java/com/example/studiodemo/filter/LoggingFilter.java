package com.example.studiodemo.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

/**
 * 日志过滤器
 * 打印请求和返回报文，统计耗时时间
 */
@Component
public class LoggingFilter implements HandlerFilterFunction<ServerResponse, ServerResponse> {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public ServerResponse filter(ServerRequest request, HandlerFunction<ServerResponse> next) throws Exception {
        long startTime = System.currentTimeMillis();

        // 记录请求信息
        logRequest(request);

        try {
            // 执行请求
            ServerResponse response = next.handle(request);

            // 记录响应信息和耗时
            long duration = System.currentTimeMillis() - startTime;
            logResponse(request, response, duration);

            return response;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[Gateway] Request failed - URI: {} - Duration: {}ms - Error: {}",
                    request.uri(), duration, e.getMessage());
            throw e;
        }
    }

    private void logRequest(ServerRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n========== Gateway Request ==========\n");
        sb.append("Method: ").append(request.method()).append("\n");
        sb.append("URI: ").append(request.uri()).append("\n");
        sb.append("Path: ").append(request.path()).append("\n");

        // 打印请求头
        sb.append("Headers:\n");
        request.headers().asHttpHeaders().forEach((name, values) -> {
            sb.append("  ").append(name).append(": ").append(String.join(", ", values)).append("\n");
        });

        sb.append("======================================");
        log.info(sb.toString());
    }

    private void logResponse(ServerRequest request, ServerResponse response, long duration) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n========== Gateway Response ==========\n");
        sb.append("URI: ").append(request.uri()).append("\n");
        sb.append("Status: ").append(response.statusCode()).append("\n");
        sb.append("Duration: ").append(duration).append("ms\n");
        sb.append("=======================================");
        log.info(sb.toString());
    }
}
