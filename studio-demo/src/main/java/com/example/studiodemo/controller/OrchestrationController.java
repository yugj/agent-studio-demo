package com.example.studiodemo.controller;

import com.example.studiodemo.model.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 编排控制器
 * 演示通过编码方式调用下游服务
 */
@RestController
@RequestMapping("/orchestration")
public class OrchestrationController {

    private static final Logger log = LoggerFactory.getLogger(OrchestrationController.class);

    private final WebClient mvcDemoWebClient;

    public OrchestrationController(WebClient mvcDemoWebClient) {
        this.mvcDemoWebClient = mvcDemoWebClient;
    }

    /**
     * 编排调用示例：调用 mvc-demo 的 /mvc/demo/hello 接口
     */
    @GetMapping("/call/hello")
    public ApiResponse<String> callHello() {
        log.info("[Orchestration] Calling mvc-demo /mvc/demo/hello");

        long startTime = System.currentTimeMillis();

        ApiResponse<String> response = mvcDemoWebClient.get()
                .uri("/mvc/demo/hello")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<String>>() {
                })
                .block();

        long duration = System.currentTimeMillis() - startTime;
        log.info("[Orchestration] Response received in {}ms: {}", duration, response);

        return response;
    }

    /**
     * 编排调用示例：调用 mvc-demo 的 /mvc/demo/hello/{name} 接口
     */
    @GetMapping("/call/hello/{name}")
    public ApiResponse<String> callHelloWithName(@PathVariable String name) {
        log.info("[Orchestration] Calling mvc-demo /mvc/demo/hello/{}", name);

        long startTime = System.currentTimeMillis();

        ApiResponse<String> response = mvcDemoWebClient.get()
                .uri("/mvc/demo/hello/{name}", name)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<String>>() {
                })
                .block();

        long duration = System.currentTimeMillis() - startTime;
        log.info("[Orchestration] Response received in {}ms: {}", duration, response);

        return response;
    }

    /**
     * 多服务编排示例：顺序调用多个接口并聚合结果
     */
    @GetMapping("/aggregate/{name}")
    public ApiResponse<AggregatedResult> aggregateCall(@PathVariable String name) {
        log.info("[Orchestration] Aggregating calls for name: {}", name);

        long startTime = System.currentTimeMillis();

        // 调用第一个接口
        ApiResponse<String> helloResponse = mvcDemoWebClient.get()
                .uri("/mvc/demo/hello")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<String>>() {
                })
                .block();

        // 调用第二个接口
        ApiResponse<String> nameResponse = mvcDemoWebClient.get()
                .uri("/mvc/demo/hello/{name}", name)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<String>>() {
                })
                .block();

        // 聚合结果
        AggregatedResult result = new AggregatedResult(
                helloResponse != null ? helloResponse.getData() : null,
                nameResponse != null ? nameResponse.getData() : null);

        long duration = System.currentTimeMillis() - startTime;
        log.info("[Orchestration] Aggregated response in {}ms", duration);

        return ApiResponse.success(result);
    }

    /**
     * 聚合结果数据类
     */
    public static class AggregatedResult {
        private String hello;
        private String name;

        public AggregatedResult() {
        }

        public AggregatedResult(String hello, String name) {
            this.hello = hello;
            this.name = name;
        }

        public String getHello() {
            return hello;
        }

        public void setHello(String hello) {
            this.hello = hello;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
