# ERP 可演进架构（erp）

把一个**遗留 ERP 单体**（42 个 Maven 模块、约 1,755 个 Controller、业务实现与契约、持久化、Web 层混杂）规范化为
**可演进的模块化架构**参考实现：5 层结构 + 端分包 + 域间只走 `api` + 包优先 + ArchUnit 护栏。

## 设计要点

| 维度 | 做法 |
|---|---|
| 分层 | `interfaces / application / domain / infrastructure / api` 五层包，单向依赖 |
| 端 | `interfaces/{web,admin,mobile,openapi,internal,device,mq,job}` 按端分包，各端 DTO 独立 |
| 域间 | 只允许依赖其它域的 `..api..` 包，由 ArchUnit 强制 |
| 模块 | 包优先：整域一个 jar；`platform` / `module` / `app` 三类聚合，按需再拆 |
| 护栏 | ArchUnit 固化分层方向、domain 纯净、Entity 不外泄、端隔离、域隔离 |

## 模块结构（35 个 Maven 工程 / 30 个 jar）

```
erp/
├─ erp-shared-kernel                    跨域共享最小模型（零框架依赖）
├─ erp-platform/                        技术底座
│  ├─ erp-platform-kernel               Result/PageResult/BizException/ErrorCode/TenantContext
│  ├─ erp-platform-persistence          BaseEntity/BaseJpaRepository/ErpJpaConfiguration
│  ├─ erp-platform-security             TokenService/AuthInterceptor/ApiAuthFilter
│  ├─ erp-platform-web                  GlobalExceptionHandler/WebMvcConfig
│  ├─ erp-platform-messaging            MessagePublisher/MessageConsumer/InMemoryMessagePublisher
│  └─ erp-platform-integration          ExternalSystemClient/HttpExternalSystemClient
├─ erp-module/                          19 个业务域（每域 1 个 jar）
│  ├─ erp-system                        基础平台（提供方，被所有域依赖）
│  ├─ erp-teaching-plan   erp-exam      erp-attendance   erp-grade      erp-student
│  ├─ erp-enrollment      erp-dormitory erp-moral-education            erp-hr
│  ├─ erp-finance         erp-asset     erp-employment   erp-evaluation erp-message
│  ├─ erp-iot-terminal    erp-quality   erp-workflow     erp-integration
├─ erp-app/                             端装配（可部署）
│  ├─ erp-app-boot                      单体组装，扫描全部端（默认入口，:8080）
│  ├─ erp-app-admin                     web + admin（:8081）
│  ├─ erp-app-mobile                    mobile（:8082）
│  └─ erp-app-openapi                   openapi + device（:8083）
└─ erp-architecture-test                ArchUnit 护栏（13 条规则）
```

## 域内包结构（以 `erp-attendance` 为例）

```
com.example.erp.attendance
├─ api              AttendanceQueryService / AttendanceClockService / dto / command
├─ interfaces       web · admin · mobile · openapi · internal · device · mq · job（各带独立 dto）
├─ application      AttendanceClockApplicationService（事务边界 + 用例编排）
├─ domain           model / repository（端口）/ service（端口）/ event
└─ infrastructure   persistence（Entity + JpaRepository + RepositoryImpl）/ client
```

`erp-attendance` 是完整样例：8 类端齐全、跨域调用 `erp-grade` 的 `api`、出站走 `erp-platform-integration`、
领域事件经 `MessagePublisher` 发布。其余 17 个域为可编译骨架。

## 依赖方向

```
erp-app-*         →  erp-module/erp-*  →  erp-platform-*  →  erp-shared-kernel
                                              ↘ erp-shared-kernel
erp-<domain>      →  其它域仅允许 import 其 ..api.. 包
erp-system        →  不依赖任何业务域（纯提供方）
```

## ArchUnit 护栏（`erp-architecture-test`）

| 测试 | 规则 |
|---|---|
| `LayerRulesTest` | 分层依赖方向；`api` 不依赖实现层 |
| `DomainPurityRulesTest` | `..domain..` 零框架依赖、不依赖其它层 |
| `DomainIsolationRulesTest` | 跨域只走 `api`；域切片无循环 |
| `EntityLeakageRulesTest` | `..infrastructure.persistence..` 不外泄；Controller 不碰仓储 |
| `EndIsolationRulesTest` | `interfaces.<端>` 之间零依赖 |
| `PlatformRulesTest` | `platform`/`shared` 不依赖业务域；`erp-system` 为纯提供方 |

## 运行与测试

```bash
cd architecture/erp
mvn test                                     # 17 个测试：4 集成 + 13 ArchUnit

mvn -pl erp-app/erp-app-boot spring-boot:run # :8080 全部端
mvn -pl erp-app/erp-app-openapi spring-boot:run  # :8083 仅 openapi + device
```

示例调用：

```bash
curl -X POST localhost:8080/api/v1/grades -H 'Content-Type: application/json' \
  -d '{"studentId":"s-1","courseName":"math","score":88}'

curl -X POST localhost:8080/api/v1/attendance/clock-in -H 'Content-Type: application/json' \
  -d '{"studentId":"s-1","clockInTime":"2026-09-16T08:00:00"}'
```

## 演进到物理拆分 / 微服务

默认整域一个 jar（文档演进路径 ②）。满足任一条件再把某域提升为 `-api` + `-core`（路径 ③）：

- 被 ≥3 个域依赖；或
- 需要独立部署；或
- 需要被本项目之外的系统依赖。

`api` 包已按契约单独隔离，提升时只需把 `api` 拆成独立 jar，业务代码零修改。
