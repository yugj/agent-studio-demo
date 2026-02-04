package com.example.studiodemo.config;

import com.example.studiodemo.filter.LoggingFilter;
import com.example.studiodemo.filter.ResponseRewriteFilter;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.function.RequestPredicate;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.net.URI;
import java.util.List;

import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.rewritePath;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates.path;
import static org.springframework.web.servlet.function.RequestPredicates.*;

/**
 * Gateway 路由配置
 * 基于 YAML 配置文件动态生成路由规则
 */
@Configuration
public class RouteConfig {

    private static final Logger log = LoggerFactory.getLogger(RouteConfig.class);

    private final GatewayProperties gatewayProperties;
    private final LoggingFilter loggingFilter;
    private final ResponseRewriteFilter responseRewriteFilter;

    public RouteConfig(GatewayProperties gatewayProperties, LoggingFilter loggingFilter,
            ResponseRewriteFilter responseRewriteFilter) {
        this.gatewayProperties = gatewayProperties;
        this.loggingFilter = loggingFilter;
        this.responseRewriteFilter = responseRewriteFilter;
    }

    @PostConstruct
    public void init() {
        log.info("========== Gateway Routes Loaded ==========");
        for (GatewayProperties.RouteDefinition routeDef : gatewayProperties.getRoutes()) {
            log.info("Route: {} - {} [{}] -> {}",
                    routeDef.getId(),
                    routeDef.getName(),
                    routeDef.getPredicates() != null ? routeDef.getPredicates().getPath() : "N/A",
                    routeDef.getUri());
        }
        log.info("============================================");
    }

    /**
     * 动态路由配置 - 简单路由（无路径重写）
     */
    @Bean
    public RouterFunction<ServerResponse> simpleRoutes() {
        List<GatewayProperties.RouteDefinition> routes = gatewayProperties.getRoutes();

        RouterFunctions.Builder builder = RouterFunctions.route();

        for (GatewayProperties.RouteDefinition routeDef : routes) {
            // 只处理没有 rewrite 配置的路由
            if (routeDef.getRewrite() == null || routeDef.getRewrite().getFrom() == null) {
                addSimpleRoute(builder, routeDef);
            }
        }

        return builder.build();
    }

    /**
     * 动态路由配置 - 路径重写路由
     * 使用 Spring Cloud Gateway MVC 的 rewritePath 功能
     */
    @Bean
    public RouterFunction<ServerResponse> rewriteRoutes() {
        List<GatewayProperties.RouteDefinition> routes = gatewayProperties.getRoutes();

        RouterFunction<ServerResponse> combined = null;

        for (GatewayProperties.RouteDefinition routeDef : routes) {
            // 只处理有 rewrite 配置的路由
            if (routeDef.getRewrite() != null && routeDef.getRewrite().getFrom() != null) {
                RouterFunction<ServerResponse> routeFunc = createRewriteRoute(routeDef);
                if (combined == null) {
                    combined = routeFunc;
                } else {
                    combined = combined.and(routeFunc);
                }
            }
        }

        return combined != null ? combined : RouterFunctions.route().build();
    }

    private void addSimpleRoute(RouterFunctions.Builder builder, GatewayProperties.RouteDefinition routeDef) {
        if (routeDef.getPredicates() == null || routeDef.getPredicates().getPath() == null) {
            return;
        }

        RequestPredicate predicate = buildPredicate(routeDef);

        builder.route(predicate, request -> {
            logRequest(routeDef, request);
            long startTime = System.currentTimeMillis();

            try {
                ServerResponse response = HandlerFunctions.http(routeDef.getUri()).handle(request);
                logResponse(routeDef, startTime);
                return response;
            } catch (Exception e) {
                log.error("Route {} failed: {}", routeDef.getId(), e.getMessage());
                throw e;
            }
        });

        log.debug("Added simple route: {}", routeDef.getId());
    }

    private RouterFunction<ServerResponse> createRewriteRoute(GatewayProperties.RouteDefinition routeDef) {
        String pathPattern = routeDef.getPredicates().getPath();
        String rewriteFrom = routeDef.getRewrite().getFrom();
        String rewriteTo = routeDef.getRewrite().getTo();

        log.debug("Creating rewrite route: {} with pattern {} -> rewrite {} to {}",
                routeDef.getId(), pathPattern, rewriteFrom, rewriteTo);

        return route(routeDef.getId())
                .route(path(pathPattern), HandlerFunctions.http(routeDef.getUri()))
                .before(rewritePath(rewriteFrom, rewriteTo))
                .filter(loggingFilter)
                .filter(responseRewriteFilter)
                .build();
    }

    private RequestPredicate buildPredicate(GatewayProperties.RouteDefinition routeDef) {
        GatewayProperties.PredicateDefinition pred = routeDef.getPredicates();

        String pathPattern = pred.getPath();
        RequestPredicate predicate = path(pathPattern);

        if (pred.getMethod() != null) {
            HttpMethod httpMethod = HttpMethod.valueOf(pred.getMethod().toUpperCase());
            predicate = predicate.and(method(httpMethod));
        }

        return predicate;
    }

    private void logRequest(GatewayProperties.RouteDefinition routeDef, ServerRequest request) {
        log.info("[{}] {} {} -> {}",
                routeDef.getId(),
                request.method(),
                request.path(),
                routeDef.getUri());
    }

    private void logResponse(GatewayProperties.RouteDefinition routeDef, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        log.info("[{}] Response in {}ms", routeDef.getId(), duration);
    }
}
