# Agent Studio Demo

基于 Spring Cloud Gateway Server MVC 实现的可代理及可编排的网关系统示例。

## 项目概述

本项目演示了如何构建一个现代化 Java 网关，支持动态路由、服务编排和响应体修改等高级特性。

## 项目结构

- **studio-demo**: 网关服务 (Port: 8080)
  - 核心网关，基于 Spring Cloud Gateway MVC
  - 负责路由转发、请求拦截、响应重写
  - 提供服务编排能力
- **mvc-demo**: 下游服务 (Port: 8081)
  - 示例 REST 服务，用于演示被代理的场景

## 核心特性

### 1. 动态路由
支持基于配置文件的动态路由定义，无需重启即可生效（需配合配置中心，本示例使用 application.yml）。
- 路径直接转发
- 路径重写转发 (RewritePath)

### 2. 响应体重写 (Response Rewrite)
实现了统一的响应体字段转换：
- `code` -> `code_t`
- `msg` -> `msg_t`
- `data` -> `data_t`

### 3. 请求日志
内置 `LoggingFilter`，打印详细的请求和响应日志，包括耗时统计。

### 4. 服务编排
演示了如何在网关层通过编码方式 (`OrchestrationController`) 调用下游服务，实现简单的服务聚合或编排。

## 快速开始

### 环境要求
- JDK 17+
- Maven 3.x

### 启动服务

1. **启动下游服务 (mvc-demo)**
```bash
cd mvc-demo
mvn spring-boot:run
```
服务将在 `http://localhost:8081` 启动。

2. **启动网关服务 (studio-demo)**
```bash
cd studio-demo
mvn spring-boot:run
```
服务将在 `http://localhost:8080` 启动。

## 接口测试

### 1. 直接访问下游服务
```bash
curl http://localhost:8081/mvc/demo/hello
# Output: {"code":"200","msg":"success","data":"yy"}
```

### 2. 通过网关访问 (带路径重写和响应修改)
```bash
curl http://localhost:8080/api/v1/demo/hello
# Output: {"code_t":"200","msg_t":"success","data_t":"yy"}
```
> 注意：返回体字段已被自动重写。

### 3. 服务编排接口
```bash
curl http://localhost:8080/orchestration/hello
# 调用网关自身 Controller，内部通过 WebClient 调用下游
```

## 性能表现

在 10 并发下持续 10 分钟压测，P99 响应时间 < 20ms，错误率 0%。
