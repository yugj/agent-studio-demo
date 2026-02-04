package com.example.studiodemo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 网关路由配置属性
 * 从 application.yml 读取路由配置
 */
@Component
@ConfigurationProperties(prefix = "gateway")
public class GatewayProperties {

    private List<RouteDefinition> routes = new ArrayList<>();

    public List<RouteDefinition> getRoutes() {
        return routes;
    }

    public void setRoutes(List<RouteDefinition> routes) {
        this.routes = routes;
    }

    /**
     * 路由定义
     */
    public static class RouteDefinition {
        private String id;
        private String name;
        private String uri;
        private PredicateDefinition predicates;
        private RewriteDefinition rewrite;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public PredicateDefinition getPredicates() {
            return predicates;
        }

        public void setPredicates(PredicateDefinition predicates) {
            this.predicates = predicates;
        }

        public RewriteDefinition getRewrite() {
            return rewrite;
        }

        public void setRewrite(RewriteDefinition rewrite) {
            this.rewrite = rewrite;
        }

        @Override
        public String toString() {
            return "RouteDefinition{id='" + id + "', name='" + name + "', path='" +
                    (predicates != null ? predicates.getPath() : "null") + "'}";
        }
    }

    /**
     * 路由断言定义
     */
    public static class PredicateDefinition {
        private String path;
        private String method;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }
    }

    /**
     * 路径重写定义
     */
    public static class RewriteDefinition {
        private String from;
        private String to;

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }
    }
}
