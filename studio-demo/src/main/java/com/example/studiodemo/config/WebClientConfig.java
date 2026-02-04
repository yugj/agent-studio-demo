package com.example.studiodemo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebClient 配置，用于编排功能调用下游服务
 */
@Configuration
public class WebClientConfig {

    @Value("${downstream.mvc-demo.url:http://localhost:8081}")
    private String mvcDemoUrl;

    @Bean
    public WebClient mvcDemoWebClient() {
        return WebClient.builder()
                .baseUrl(mvcDemoUrl)
                .build();
    }
}
