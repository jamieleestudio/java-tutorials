# embabel-state-machine — LLM 驱动的状态机

## 演示内容

订单处理流程被建模成一个**显式状态机**，工具**按状态收敛**：

```
NEW --validateOrder--> VALIDATED --chargePayment--> PAID --shipOrder--> SHIPPED
 |
 +---rejectOrder--> REJECTED
```

- 处于 `NEW` 时，模型**只能看到** `validateOrder` / `rejectOrder`
- 校验通过并转移到 `VALIDATED` 后，`chargePayment` 才出现
- 因此"未支付先发货"这类非法顺序**在工具层面就不存在**

实测工具分布：`{NEW=2, VALIDATED=1, PAID=1}`，调用序列：

```
[state=NEW]       validateOrder
[state=VALIDATED] chargePayment
[state=PAID]      shipOrder
```

## 关键 API

| API | 作用 |
|---|---|
| `new StateMachineTool<>(name, description, StateEnum.class)` | 创建状态机工具（`S extends Enum<S>`） |
| `.withInitialState(S)` / `.startingIn(S)` | 起始状态（后者用于运行时动态指定） |
| `.withStateAwareSystemPrompt((ctx, input, state) -> ...)` | 自定义系统提示（能拿到当前状态） |
| `.withGlobalTool(tool)` / `.withGlobalTools(...)` | 所有状态都可用的工具 |
| `.withMaxIterations(n)` | 内部工具循环上限 |
| `.inState(S).withTool(tool).transitionsTo(S2)` | 注册"某状态可用、执行后转移到 S2"的工具 |
| `machine.call(input)` | 直接驱动状态机（也可作为 `Tool` 交给外层 LLM 调用） |

> **重要陷阱**：`transitionsTo(S2)` 返回的仍是**源状态**的 builder
> （源码：`StateBuilder(state, updated)`），所以每次转移后必须显式 `.inState(S2)`，
> 否则后续工具会被注册到源状态。本模块第一版就踩了这个坑——
> 结果 4 个工具全在 `NEW`，模型自己诊断出"支付/发货在 VALIDATED 不可用，形成死局"。
> 这从反面证明了状态机约束确实是**硬约束**。

## 接口

```bash
curl -G --data-urlencode "order=处理订单 A1001：客户张三，金额 ¥299，要求今天发货" \
     http://localhost:8929/state-machine/process
```

返回：`{"request":..., "output":"...", "toolsPerState":{"NEW":2,"VALIDATED":1,"PAID":1}}`

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-state-machine spring-boot:run
```

## 代码结构

- `OrderState.java` — 状态枚举（含状态图注释）
- `OrderWorkflowAgent.java` — `buildMachine()` 组装状态机 + `@Action` 直接 `machine.call(...)`
- `OrderResult` — 结果（含各状态工具数，便于观察收敛效果）
- `StateMachineController.java` — `GET /state-machine/process`

## 要点（与相近模式的区别）

- vs **`embabel-guardrails` / `embabel-secure-tools`**：那两者在**执行前/后**做校验与权限拦截；
  状态机是**按阶段**收敛工具的可见性。
- vs **`embabel-workflows`**：工作流是编译期固定的动作图；状态机是"运行时状态 + 模型在约束内自主选择"。
- 框架里还有另一种"状态"机制：`@State` 类——把状态对象作为**类型化对象放进黑板**，
  由 `@Action` 方法读写（见 `com.embabel.agent.api.annotation.State`）。
  两者取舍：`StateMachineTool` 适合"阶段化工具可见性"，`@State` 适合"状态作为领域对象"。
