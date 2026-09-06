# ⑤ 微服务架构 (Microservices Architecture)

## 概述

③ 裸分布式的**治理化升级**：Nacos 注册/配置中心 + OpenFeign + LoadBalancer + Resilience4j 熔断 + Spring Cloud Gateway + Micrometer 链路追踪。每个上下文独立部署、独立数据库。

## 与 ③ 的核心差异（治理生态全景）

| 能力 | ③ 裸分布式 | ⑤ 微服务 |
|---|---|---|
| 服务发现 | 固定地址配置 | **Nacos 注册中心** |
| 配置管理 | 本地 yml | **Nacos 配置中心**（optional 导入，缺失可启动） |
| 负载均衡 | 无 | **Spring Cloud LoadBalancer**（客户端） |
| RPC | RestClient 手写 | **OpenFeign 声明式 + 熔断降级** |
| 网关 | 无直连 | **Spring Cloud Gateway**（8080 统一入口） |
| 熔断 | 无 | **Resilience4j**（滑动窗口/半开/超时） |
| 链路追踪 | 无 | **Micrometer Tracing + Zipkin** |

## 模块结构（13 个）

```
microservices/
├── shared-kernel/
├── order-{domain,application,bootstrap}        bootstrap 含 feign/{PaymentClient, ProductClient + Fallback + FeignClient适配}
├── payment-{api,domain,application,bootstrap}
├── product-{api,domain,application,bootstrap}
└── api-gateway/                                SCG + Nacos: /api/** → lb://service-name
```

## ③→⑤ 演进点

```
③: PaymentServiceRpcClient（RestClient + 手写重试 + 固定URL）
⑤: PaymentServiceFeignClient implements PaymentService
      └─ PaymentClient(@FeignClient name="payment-service") ← Nacos发现+LB+Resilience4j熔断+fallback
```
OrderServiceImpl 依然**零修改**——依赖反转贯穿 ②→③→⑤。

## 熔断配置（order-bootstrap/application.yml）

- `spring.cloud.openfeign.circuitbreaker.enabled: true` — 每次 Feign 调用包裹熔断器
- Resilience4j：`payment-service`/`product-service` 实例，失败率 50% 熔断 10s，超时 3s
- Fallback 抛业务异常（`PAYMENT_SERVICE_UNAVAILABLE`），不向调用方泄漏传输异常

## 运行（需 Nacos）

```bash
# 1. 启动 Nacos (docker run -p 8848:8848 nacos/nacos-server)
cd architecture/microservices
mvn spring-boot:run -pl payment-bootstrap &    # 注册到 Nacos
mvn spring-boot:run -pl product-bootstrap &
mvn spring-boot:run -pl order-bootstrap &
mvn spring-boot:run -pl api-gateway           # 8080 统一入口
# NACOS_ADDR 环境变量覆盖默认 localhost:8848
```

经网关访问：`http://localhost:8080/api/orders`、`/api/products`、`/api/payments`

## 测试

```bash
mvn test   # 15 个：8 领域 + 1 流程(@MockBean远程服务+Nacos禁用) + 6 ArchUnit
```
测试资源 `application.yml` 禁用 Nacos/熔断——无需外部设施即可跑集成测试。