# embabel-hitl-advanced — 工具级 HITL（按需索要强类型输入）

## 演示内容

把"向用户要输入"**下沉到工具调用点**，并且**只在需要时才问**：

| 场景 | 行为 |
|---|---|
| 金额 500（≤ 上限 1000） | **不问**，用默认账号直接完成 → `COMPLETED` |
| 金额 5000（> 上限） | 停在 `WAITING`，返回"需要 `RefundAccount`" → 用户补账号 → `COMPLETED` |
| 再次调用（已提供过账号） | **不再问第二次** → 直接执行 |

实测：

```json
// 小额：自动完成
{"status":"COMPLETED","output":{"orderId":"A1001","amount":500.0,"askedUser":false,
 "result":"退款已发起：{...\"account\":\"自动审批-默认账号\"}"}}

// 大额：等待用户输入
{"status":"WAITING","awaitableType":"TypeRequest","requestedType":"RefundAccount",
 "message":"退款金额 5000 超过自动审批上限 1000，请提供退款账号信息",
 "hint":"POST /hitl-advanced/eloquent_colden/account，body 为 RefundAccount 的 JSON"}

// 提交账号后恢复
{"status":"COMPLETED","responseImpact":"UPDATED",
 "output":{"orderId":"A2002","amount":5000.0,"askedUser":true,
 "result":"退款已发起：{...\"account\":\"6222 0000 1234 5678\"}"}}
```

日志印证 decider 每次都被问到：

```
RefundAgent - 金额 500.0 ≤ 1000.0，自动审批，不打扰用户
RefundAgent - 金额 5000.0 > 1000.0，向用户索取退款账号
TypeRequest - Received type response: RefundAccount[account=6222 0000 1234 5678, bank=招商银行, holder=张三]
RefundAgent - 用户已提供退款账号，直接执行
```

## 关键 API

| API | 作用 |
|---|---|
| `new ConditionalAwaitingTool(tool, decider)` | 包装工具：调用前先问 decider"要不要等用户" |
| `AwaitDecider.evaluate(AwaitContext)` | 返回 `Awaitable` → 抛异常进入 WAITING；返回 `null` → 直接执行 |
| `AwaitContext` | 拿到 `getInput()`（工具入参）、`getAgentProcess()`（可查黑板）、`getTool()` |
| `new TypeRequest<>(T.class, message)` | 向用户索取一个 `T`（payload 是 `Class<T>`，UX 层据此生成表单） |
| `Awaitable.onResponse(TypeResponse, process)` | 响应协议：由 awaitable 自己把值写进黑板，返回 `ResponseImpact` |
| `new TypeResponse<>(value, awaitableId, id, Instant.now(), false)` | 响应对象（Java 只能调这个全参构造器，Kotlin 默认值在 Java 侧不可见） |
| `ConfirmingTool` / `FormBindingRequest` / `ValidationError` | 无条件确认 / 表单绑定（可带校验错误）——见文末 |
| `@Action(canRerun = true)` | **必需**，见下 |

## 接口

```bash
# 小额：自动完成
curl -G --data-urlencode "message=退款订单 A1001 金额 500" http://localhost:8938/hitl-advanced/refund

# 大额：停在 WAITING，拿到 processId
curl -G --data-urlencode "message=退款订单 A2002 金额 5000" http://localhost:8938/hitl-advanced/refund

# 提交账号恢复（Windows/PowerShell 注意：JSON body 要写文件后用 --data-binary "@file"，
# 直接 -d '{"a":"b"}' 会被 PowerShell 吞掉双引号，服务端报 JSON parse error）
curl -X POST -H "Content-Type: application/json" \
     --data-binary "@acct.json" http://localhost:8938/hitl-advanced/{processId}/account
```

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-hitl-advanced spring-boot:run
```

> 本模块**不需要 LLM / API Key**：整条链路是确定性的（直接调用被包装的工具）。

## 代码结构

- `RefundAgent.java` — `ConditionalAwaitingTool` + decider（阈值判断 + 已提供则不重复问）
- `RefundAccount` — 中途向用户索要的强类型输入（字段描述会成为表单提示）
- `RefundOutcome` — 目标类型（含 `askedUser`，便于观察"到底问没问"）
- `HitlAdvancedController.java` — `/hitl-advanced/refund`、`/hitl-advanced/{processId}/account`

## ⚠️ 两个必须知道的坑

### 1. 动作必须 `canRerun = true`，否则恢复后直接 STUCK

第一版没加，结果：`onResponse` 成功写入了 `RefundAccount`（日志可见 `responseImpact=UPDATED`），
但进程状态是 **`STUCK`** 而不是 `COMPLETED`。

原因：动作在进入等待时已经被记为"执行过"，框架**默认不会重跑它**（`canRerun=false`）；
而 `TypeRequest` 补进来的 `RefundAccount` 并不满足目标 `RefundOutcome`，于是规划器找不到路径 → STUCK。

> 这也解释了为什么 `embabel-hitl`（用 `WaitFor.confirmation/formSubmission`）不需要 `canRerun`：
> 它的 awaitable payload **本身就是目标类型**（例如 `WaitFor.confirmation(new ChatReply(draft), ...)`），
> 恢复时把 payload 加进黑板就等于满足了目标，动作不需要再跑。
> 而工具级 HITL 是"补一个前提，然后**继续执行**"，所以必须允许重跑。

### 2. 别直接用 `TypeRequestingTool`——它会无限重复等待

`TypeRequestingTool.call()` 的实现是**无条件**抛异常：

```kotlin
override fun call(input: String, context: ToolCallContext): Tool.Result {
    throw AwaitableResponseException(TypeRequest(type = type, message = messageProvider(input)))
}
```

而 `AwaitableResponseException` 的处理路径（`ActionRunner`）只是把 awaitable 加到黑板并置为 `WAITING`，
**没有任何"这个类型已经问过了"的去重**。所以恢复后再次调用该工具会再次抛异常 → 又 WAITING → 死循环。

所以本模块用 `ConditionalAwaitingTool` + **自己守卫的 decider**：
先查黑板（`process.last(RefundAccount.class) != null`）再决定要不要问。
如果你确实要用 `TypeRequestingTool`，请自己包一层同样的守卫。

## 要点

- **粒度对比**：`embabel-hitl` 在**动作内部**等输入（`WaitFor`），本模块在**工具调用点**等。
  后者更细：同一个动作里可以只有某一个工具需要问，且能按运行时条件决定要不要问。
- **"按需打扰"是产品体验的关键**：小额自动通过、大额才要账号——把 HITL 放在阈值判断之后，
  而不是每个操作都弹确认框。
- **响应走对象协议**：`awaitable.onResponse(response, process)` 而不是直接往黑板塞对象。
  awaitable 自己决定如何更新进程状态（返回 `ResponseImpact.UPDATED`/`UNCHANGED`），
  这样"响应如何影响流程"的逻辑留在 awaitable 里，控制器只负责传值。
- 其他相关 API（本模块未演示，可按需扩展）：
  - `ConfirmingTool` / `Tool.withConfirmation(...)` —— 无条件要求确认（危险操作固定确认，
    已在 `embabel-secure-tools` 演示过类似效果）。
  - `FormBindingRequest` + `ValidationError` —— 表单绑定，并携带**校验错误**让 UX 重新提示
    （注意它的构造器带 `List<ValidationError>`，但 `ValidationError` 是个空标记接口，
    真正校验要自己实现）。
  - `SimpleAwaitableTypedTool` / `AwaitableTypedTool` —— 把"等待 + 执行"封装成一个 `TypedTool`，
    适合"工具本身就代表一次人机交互"的场景。
- **UX 契约**：`TypeRequest` 的 payload 是 `Class<T>`，意味着"这个类型即表单 schema"。
  把领域类型设计好（字段名 + `@JsonPropertyDescription`），前端表单就能自动生成。
