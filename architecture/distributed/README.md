# ③ 分布式架构 (Distributed Architecture — 演进过渡版)

## 概述

② 的演进：1 个 JVM 拆成 **3 个独立进程**。聚焦"从单体到分布式第一步"的技术挑战：**远程调用替换、分布式幂等、超时重试、补偿**。最小治理（固定地址配置，无注册中心——留给 ⑤）。

## 与 ② 的核心差异

| | ② 单体多模块 | ③ 分布式 |
|---|---|---|
| JVM | 1 个 | **3 个**（8081 order / 8082 payment / 8083 product） |
| 跨上下文调用 | 进程内注入 | **HTTP RPC（RestClient）** |
| domain/application | — | **零修改**（只换 bootstrap 的 infrastructure） |
| 幂等 | 不需要 | **orderId 唯一 + 状态判断**（RPC 重试安全） |
| 超时重试 | 不需要 | connect 2s / read 3s + 2 次重试 |

## 模块结构（12 个）

```
distributed/
├── shared-kernel/
├── order-domain/            与②完全相同
├── order-application/       与②完全相同（仍注入 PaymentService/ProductService 接口）
├── order-bootstrap/         OrderApplication + Controller + JPA + rpc/{PaymentServiceRpcClient, ProductServiceRpcClient}
├── payment-api/             跨上下文契约：PaymentService(pay/refund) + PaymentDto
├── payment-domain/          与②相同
├── payment-application/     PaymentAppService extends PaymentService + PaymentServiceImpl
├── payment-bootstrap/       PaymentApplication + Controller + PaymentRpcController(/rpc/**) + JPA
├── product-*（同 payment 结构 ×4）
```

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
mvn spring-boot:run -pl payment-bootstrap &   # 8082
mvn spring-boot:run -pl product-bootstrap &   # 8083
mvn spring-boot:run -pl order-bootstrap       # 8081
```

服务地址通过环境变量覆盖：`PAYMENT_SERVICE_URL` / `PRODUCT_SERVICE_URL`

## 测试

```bash
mvn test   # 15 个：8 领域 + 1 流程(@MockBean 远程服务) + 6 ArchUnit
```

`OrderServiceFlowTest` 演示了提供方接口的测试优势：远程服务 mock 掉即可测完整流程，无需起进程。