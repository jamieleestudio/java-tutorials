# embabel-identity — 用户身份与请求级元数据

## 演示内容

两件事，都是"Agent 在生产里必须知道、但模型提示词里不该出现"的信息：

1. **身份（谁在问 / 以谁的身份执行）** —— `Identities` + `User` + `UserService`
2. **请求级元数据（这次请求属于哪个租户）** —— `ToolCallContext`，带外透传到每个工具

并刻意给出**反面教材**：框架负责把元数据送到工具手上，但**"用不用它"由你的代码决定**。

## 实测（同一套数据，三种调用）

| 请求 | `forUser` | `runAs` | `toolCallContext` | 租户隔离工具 | 不读上下文的工具（反面） |
|---|---|---|---|---|---|
| `/identity/whoami?username=alice` | alice | svc-agent | `{}` | 租户 unknown → `[]` | `A1001, A1002, A1003` |
| `/identity/orders?username=alice&tenant=acme` | alice | svc-agent | tenantId=acme | `[A1001(500), A1003(1200)]` | `A1001, A1002, A1003` |
| `/identity/orders?username=bob&tenant=globex` | bob | svc-agent | tenantId=globex | `[A1002(800)]` | `A1001, A1002, A1003` |

```json
// /identity/orders?username=alice&tenant=acme
{"forUser":{"id":"u-1001","username":"alice","displayName":"Alice（Acme 采购）","email":"alice@acme.example"},
 "runAs":{"id":"svc-1","username":"svc-agent","displayName":"Service Account"},
 "toolCallContext":{"tenantId":"acme","userId":"u-1001","requestId":"8440c7cd"},
 "scopedResult":"租户 acme 的订单：[A1001(500), A1003(1200)]（requestId=8440c7cd）",
 "leakyResult":"A1001, A1002, A1003"}
```

日志印证身份与上下文都到了动作里：

```
IdentityAgent - forUser=alice, runAs=svc-agent, toolCallContext={}
IdentityAgent - forUser=alice, runAs=svc-agent, toolCallContext={tenantId=acme, userId=u-1001, requestId=8440c7cd}
IdentityAgent - forUser=bob, runAs=svc-agent, toolCallContext={tenantId=globex, userId=u-1002, requestId=4de8f2fb}
```

## 关键 API

| API | 作用 |
|---|---|
| `new Identities(User forUser, User runAs)` | **为谁执行** + **以谁的身份执行**（服务账号，用于权限委派） |
| `ProcessOptions.withIdentities(Identities)` | 把身份传进这次运行 |
| `OperationContext.user()` | 动作里拿当前用户（`OperationContext` 参数由框架自动注入） |
| `User` / `SimpleUser(id, displayName, username, email)` | 用户抽象与默认实现 |
| `UserService<U>` | `findById` / `findByUsername` / `findByEmail` / `provisionUser`（对接你的账号体系） |
| `ProcessOptions.withToolCallContext(Map<String, Object>)` | **请求级**元数据（有 Map 重载，Java 用起来很顺手） |
| `ToolCallContext.get(key)` / `getOrDefault(key, default)` / `toMap()` / `loopId()` | 工具里读取元数据 |
| `Tool.call(String, ToolCallContext)` | 工具接收上下文的入口（`Tool.create` 的 handler **拿不到**它，需自己实现 `Tool`） |
| `@Action` 方法参数写 `OperationContext` / `ProcessContext` | 框架的 `OperationContextArgumentResolver` 支持注入 |

> **两个容易踩的点**：
> 1. `Tool.create(name, desc, handler)` 的 handler 只收到入参字符串，**没有** `ToolCallContext`。
>    要读上下文必须自己实现 `Tool` 并覆写 `call(String, ToolCallContext)`
>    （框架内部的 `ContextAwareFunctionalTool` 是包级可见的，外部用不了）。
> 2. 动作里别去找线程本地变量——直接声明 `OperationContext` 参数即可，
>    框架的判断条件是 `OperationContext.isAssignableFrom(参数类型)`，所以 `ActionContext` 也能注入。

## 接口

```bash
curl -G --data-urlencode "username=alice" http://localhost:8939/identity/whoami
curl -G --data-urlencode "username=alice" --data-urlencode "tenant=acme" http://localhost:8939/identity/orders
curl -G --data-urlencode "username=bob"   --data-urlencode "tenant=globex" http://localhost:8939/identity/orders
```

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-identity spring-boot:run
```

> 本模块**不需要 LLM / API Key**：身份与上下文透传是确定性的，直接调用工具即可验证。

## 代码结构

- `InMemoryUserService.java` — `UserService<SimpleUser>` 实现（含一个 `svc-agent` 服务账号）
- `OrderTools.java` — 两个对照工具：`tenantScopedQuery()`（读上下文）与 `leakyQuery()`（不读）
- `IdentityAgent.java` — 动作注入 `OperationContext`，取身份 + 把上下文**显式**传给工具
- `IdentityController.java` — 用 `ProcessOptions.withIdentities(...).withToolCallContext(...)` 组装运行
- `IdentityReport` / `Order` — 报告与数据模型

## 要点

- **为什么元数据要走带外通道**：把租户 ID / token 写进提示词既会被模型复述（泄露），
  也不可靠（可能被改写）。`ToolCallContext` 让它们随请求走代码路径，模型看不见。
- **MCP 也能拿到**：框架会把它桥接到 Spring AI 的 `ToolContext`，最终映射到 MCP 的 `McpMeta`，
  所以跨进程的 MCP 工具同样能拿到租户/请求 ID。
- **`forUser` vs `runAs` 是两件事**：前者是"业务主体"（决定数据可见范围与审计归属），
  后者是"执行身份"（决定技术权限）。典型用法是 runAs 一个最小权限服务账号，
  再按 forUser 做数据过滤——本模块两个都返回了，便于观察。
- **⚠️ 隔离必须显式实现**：`leakyQuery()` 证明了框架**不会**替你过滤数据。
  只声明 `ToolCallContext` 而没有在工具里用它，等于没有隔离。
  真实系统里应把它做成**强制**机制（例如所有仓储查询都从上下文取租户，而不是作为可选参数）。
- **`loopId()`**：上下文里还有一个框架自带的 `loopId`，用于区分同一进程内不同工具循环轮次
  （`ToolCallContext.LOOP_ID_KEY`），做审计/去重时有用。
- 与相邻模块的分工：
  - `embabel-byok`（⑦）管**模型访问**的租户隔离（每租户 API Key 与成本）；
  - `embabel-identity`（本模块）管**数据访问**的租户隔离与身份透传。两者互补。
  - `embabel-secure-tools`（⑤）管"危险操作前确认 + PII 护栏"；
    本模块管"这个请求是谁、能看到哪些数据"。
