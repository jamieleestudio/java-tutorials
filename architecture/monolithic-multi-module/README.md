# ② 单体多模块架构 (Monolithic Multi-Module Architecture)

## 概述

① 的演进：3 个限界上下文拆为独立 **Maven 模块**（编译期强制上下文边界），4 层仍用包 + ArchUnit 守护，最终打包为 **1 个 fat jar**（1 个 JVM）。

## 与 ① 的核心差异

| | ① 单体 | ② 单体多模块 |
|---|---|---|
| 上下文边界 | 包级别（靠 ArchUnit） | **Maven 模块级别（编译期强制）** |
| 部署单元 | 1 jar | 1 fat jar（bootstrap 组装） |
| 4 层隔离 | 包 + ArchUnit | 包 + ArchUnit（不变） |

## 模块结构

```
monolithic-multi-module/
├── pom.xml                 (聚合)
├── shared-kernel/          纯 Java 内核，零框架依赖
├── order/                  com.example.mmm.order (interfaces/application/domain/infrastructure)
├── product/                com.example.mmm.product
├── payment/                com.example.mmm.payment
└── bootstrap/              启动组装：Application + GlobalExceptionHandler + 配置 → 1 个 fat jar
```

## Maven 依赖方向（编译期单向）

```
bootstrap → order → payment, product → shared-kernel
                    product ──────↗
                    payment ─────↗
```

- `order` 依赖 `payment`/`product`（提供方定义的 Service 接口）
- `payment`/`product` 互不依赖、不依赖 order（ArchUnit + Maven 双重保证）

## 命名规范

与 ① 完全一致（Service/ServiceImpl、Command/Query/Dto、Request/Response），详见 `architecture/monolithic/README.md`。

## 运行与测试

```bash
cd architecture/monolithic-multi-module
mvn spring-boot:run -pl bootstrap      # http://localhost:8080
mvn test                               # 18 个测试（8 领域 + 1 集成 + 9 ArchUnit）
```

## 演进到 ③

③ 的 order-domain/order-application 代码与本模块**完全相同**（零修改）——只把 bootstrap 里的进程内调用换成 RPC 客户端。这是上下文模块化带来的可演进性。