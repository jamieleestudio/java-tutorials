# ③ 分布式架构 (Distributed Architecture — 演进过渡版)

## 概述

② 的演进：1 个 JVM 拆成 **3 个独立进程**。聚焦"从单体到分布式第一步"的技术挑战：**远程调用替换、分布式幂等、超时重试、补偿**。最小治理（固定地址配置，无注册中心——留给 ⑤）。

## 与 ② 的核心差异

| | ② 单体多模块 | ③ 分布式 |
|---|---|---|
| JVM | 1 个 | **3 个**（8081 order / 8082 payment / 8083 product） |
| 跨上下文调用 | 程内注入 | **HTTP RPC（RestClient）** |
| domain/application | — | **零修改**（只换 infrastructure 的 RPC 客户端） |
| 幂等 | 不需要 | **orderId 唯一 + 状态判断**（RPC 重试安全） |
| 超时重试 | 不需要 | connect 2s / read 3s + 2 次重试 |

## 模块结构（8 个）

```
distributed/
├── shared-kernel/          纯 Java 内核，零框架依赖
├── payment/                业务域聚合：api + service
│   ├── payment-api/        跨服务契约：PaymentService(pay/refund) + PaymentDto
│   └── payment-service/    单模块 4 层：domain + application + interfaces + infrastructure
├── product/                业务域聚合：api + service
│   ├── product-api/        跨服务契约：ProductService + ProductDto
│   └── product-service/    单模块 4 层：domain + application + interfaces + infrastructure
└── order-service/          单模块 4 层：domain + application + interfaces + infrastructure/rpc
```

### 模块拆分粒度（业界对齐）

本架构采用 **2 模块/服务**（api + service），service 内 4 层用包 + ArchUnit 守护。
这是 Spring 官方团队、Spring Cloud samples、Dubbo、eShopOnContainers 等业界主流做法。

另一种更细粒度的做法是 **4 模块/服务**（api + domain + application + bootstrap），
用 Maven 编译期强制层隔离。Vernon《Implementing DDD》教学项目采用此风格
生产项目中仅见于金融/银行等强合规场景。

选择 2 模块的理由：
- ArchUnit 已覆盖 domain 纯度、层方向、跨上下文隔离等约束
- 模块数从 12 降至 8，降低构建复杂度
- 与 ④ 事件驱动结构一致，演进谱系风格统一

## ②→③ 演进点（唯一改动处）

```
②: OrderServiceImpl ← 注入 PaymentService ← PaymentApplicationService(@Service 进程内)
③: OrderServiceImpl ← 注入 PaymentService ← PaymentServiceRpcClient(RestClient 跨进程)  ← 只换这个类
```

## API 契约

**对外 REST**：`/api/orders/**`、`/api/products/**`、`/api/payments/**`
**内部 RPC**（跨服务）：
- `POST /rpc/payments/pay?orderId&amount` → paymentId（幂等：同 orderId 返回同 paymentId）
- `POST /rpc/payments/{id}/refund`
- `GET /rpc/products?ids=a,b` / `GET /rpc/products/{id}`

## 运行

```bash
cd architecture/distributed
mvn spring-boot:run -pl :dist-payment-service &   # 8082
mvn spring-boot:run -pl :dist-product-service &   # 8083
mvn spring-boot:run -pl :dist-order-service       # 8081
```

服务地址通过环境变量覆盖：`PAYMENT_SERVICE_URL` / `PRODUCT_SERVICE_URL`

## 测试

```bash
mvn test   # 18 个：8 领域 + 1 流程(@MockBean 远程服务) + 9 ArchUnit
```

`OrderServiceFlowTest` 演示了提供方接口的测试优势：远程服务 mock 掉即可测完整流程，无需起进程。