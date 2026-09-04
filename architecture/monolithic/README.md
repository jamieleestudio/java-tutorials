# ① 单体架构 (Monolithic Architecture)

## 概述

单体架构示例：1 个 Maven 模块、1 个 JVM、1 个数据库，通过**包边界**实现 DDD 清洁架构（4 层包结构）+ 3 个限界上下文。

## 包结构（业务优先）

```
com.example.monolithic/
├── shared/                              共享内核
│   ├── AggregateRoot.java               聚合根基类
│   ├── DomainEvent.java                 领域事件接口
│   ├── DomainException.java             领域异常基类
│   ├── BusinessRuleViolationException.java
│   ├── EntityNotFoundException.java
│   ├── IdGenerator.java
│   └── GlobalExceptionHandler.java      统一异常处理
│
├── order/                               订单上下文
│   ├── interfaces/                       接口层
│   │   ├── web/                          OrderController, Request/Response DTO
│   │   └── event/                        OrderEventListener
│   ├── application/                      应用层
│   │   ├── OrderService.java             服务接口
│   │   ├── dto/                          OrderDto (应用层输出契约)
│   │   ├── command/                      写操作入参 + EventPublisherService
│   │   ├── query/                        查询入参
│   │   └── impl/                         OrderServiceImpl
│   ├── domain/                           领域层（纯 POJO，零框架依赖）
│   │   ├── Order.java                    聚合根
│   │   ├── OrderItem.java                值对象
│   │   ├── OrderStatus.java              枚举（状态机）
│   │   ├── OrderRepository.java          仓储接口
│   │   └── event/                        领域事件
│   └── infrastructure/                   基础设施层
│       ├── OrderEntity / OrderItemEmbeddable   JPA 实体
│       ├── OrderJpaRepository                  Spring Data JPA
│       ├── OrderRepositoryImpl                 仓储实现
│       └── SpringEventPublisherServiceImpl    事件发布实现
│
├── product/                             商品上下文（同结构）
└── payment/                             支付上下文（同结构）
```

## 命名规范

| 层 | 用途 | 后缀 | 示例 |
|---|---|---|---|
| interfaces/web | 请求 DTO | `Request` | `CreateOrderRequest` |
| interfaces/web | 响应 DTO | `Response` | `OrderResponse` |
| application | 服务接口 | `Service` | `OrderService` |
| application/impl | 服务实现 | `ServiceImpl` | `OrderServiceImpl` |
| application/command | 写操作入参 | `Command` | `CreateOrderCommand` |
| application/query | 查询入参 | `Query` | `GetOrderByIdQuery` |
| application/dto | 应用层输出 | `Dto` | `OrderDto` |
| domain | 聚合根/值对象 | 无后缀 | `Order`、`OrderItem` |
| domain | 仓储接口 | `Repository` | `OrderRepository` |
| domain/event | 领域事件 | `Event` | `OrderCreatedEvent` |
| infrastructure | JPA 实体 | `Entity` | `OrderEntity` |
| infrastructure | Spring Data 接口 | `JpaRepository` | `OrderJpaRepository` |
| infrastructure | 仓储实现 | `Impl` | `OrderRepositoryImpl` |

## 数据流

```
Request (Web) → Command/Query (Application) → Service 接口 → ServiceImpl → Dto (Application) → Response (Web)
```

## 4 层依赖方向

```
interfaces → application (Service 接口)
                application.impl → domain
                infrastructure → domain (依赖反转：实现 domain 定义的 Repository 接口)
```

- **interfaces**：Controller、EventListener，只注入 application 层的 Service **接口**（不碰 impl）
- **application**：用例编排、事务边界；command/query/dto/impl 四个子包
- **domain**：聚合根、值对象、领域事件、仓储接口，**零框架依赖**
- **infrastructure**：JPA 实现、事件发布实现，实现 domain/application 定义的接口

## 跨上下文通信（提供方定义接口）

跨上下文接口由**提供方**定义，与 Maven 依赖方向一致（order → payment/product）：

```
order.application.impl.OrderServiceImpl
    ├─ 注入 ProductService (product 包定义的接口)
    ├─ 注入 PaymentService (payment 包定义的接口)
    └─ 演进到 ③ 分布式时，order 侧 infrastructure 加
       PaymentServiceRpcClient implements PaymentService (Feign)，
       OrderServiceImpl 代码零修改
```

## 架构守护（ArchUnit，12 条规则）

- domain 零框架依赖（不依赖 Spring/Jakarta）
- domain 不依赖 application/infrastructure/interfaces
- interfaces 不访问 infrastructure、不注入 ServiceImpl（只依赖接口）
- infrastructure 不访问 interfaces、不跨上下文
- 跨上下文：order 只访问 product/payment 的 application 层接口
- product/payment 不依赖 order；product 和 payment 互不依赖
- bounded context 无循环依赖
- shared kernel 不依赖任何上下文

## API 端点

| Method | Path | 说明 |
|--------|------|------|
| POST | `/api/products` | 创建商品 |
| GET | `/api/products` | 查询所有商品 |
| GET | `/api/products/{id}` | 查询单个商品 |
| POST | `/api/orders` | 创建订单 |
| POST | `/api/orders/{id}/pay` | 支付订单 |
| POST | `/api/orders/{id}/cancel` | 取消订单（body: `{"reason": "..."}`） |
| GET | `/api/orders/{id}` | 查询订单 |
| GET | `/api/orders?customerId=` | 按客户查询订单 |
| POST | `/api/payments` | 创建支付 |
| POST | `/api/payments/{id}/process` | 处理支付 |
| GET | `/api/payments/{id}` | 查询支付 |
| GET | `/api/payments` | 查询所有支付 |

## 运行

```bash
cd architecture/monolithic
mvn spring-boot:run
```

H2 Console: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:monolith`)

## 测试

```bash
mvn test
```

- `OrderTest`：领域单元测试（8 个）
- `MonolithicIntegrationTest`：全流程集成测试（创建商品→创建订单→支付）
- `ArchitectureRulesTest`：ArchUnit 架构守护测试（12 个）