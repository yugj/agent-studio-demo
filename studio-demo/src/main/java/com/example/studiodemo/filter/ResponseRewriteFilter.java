package com.example.studiodemo.filter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.server.mvc.filter.AfterFilterFunctions;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 响应体重写过滤器
 * 将返回报文中的 code, msg, data 字段重写为 code_t, msg_t, data_t
 * 
 * 使用 Spring Cloud Gateway MVC 的 modifyResponseBody 功能
 */
@Component
public class ResponseRewriteFilter implements HandlerFilterFunction<ServerResponse, ServerResponse> {

    private static final Logger log = LoggerFactory.getLogger(ResponseRewriteFilter.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ServerResponse filter(ServerRequest request, HandlerFunction<ServerResponse> next) throws Exception {
        // 使用 Spring Cloud Gateway MVC 提供的 modifyResponseBody after filter
        // 返回类型是 BiFunction<ServerRequest, ServerResponse, ServerResponse>
        var afterFilter = AfterFilterFunctions.modifyResponseBody(
                String.class,
                String.class,
                null, // 不改变 Content-Type
                (serverRequest, serverResponse, body) -> rewriteResponseBody(body));

        // 执行下游请求获取原始响应
        ServerResponse originalResponse = next.handle(request);

        // 应用 after filter 修改响应体
        return afterFilter.apply(request, originalResponse);
    }

    /**
     * 重写响应体，将 code/msg/data 转换为 code_t/msg_t/data_t
     */
    private String rewriteResponseBody(String body) {
        if (body == null || body.isEmpty()) {
            return body;
        }

        try {
            log.debug("Original response body: {}", body);

            // 解析 JSON
            Map<String, Object> originalMap = objectMapper.readValue(
                    body, new TypeReference<Map<String, Object>>() {
                    });

            // 创建新的 Map，保持字段顺序
            Map<String, Object> rewrittenMap = new LinkedHashMap<>();

            for (Map.Entry<String, Object> entry : originalMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                // 重写字段名
                String newKey = switch (key) {
                    case "code" -> "code_t";
                    case "msg" -> "msg_t";
                    case "data" -> "data_t";
                    default -> key;
                };

                rewrittenMap.put(newKey, value);
            }

            String rewrittenBody = objectMapper.writeValueAsString(rewrittenMap);
            log.debug("Rewritten response body: {}", rewrittenBody);

            return rewrittenBody;
        } catch (Exception e) {
            log.warn("Failed to rewrite response body, returning original: {}", e.getMessage());
            return body;
        }
    }
}
