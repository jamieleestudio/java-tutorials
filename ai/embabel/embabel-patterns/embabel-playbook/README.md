# embabel-playbook — 解锁条件式工具集

## 演示内容

把**流程约束**表达成"工具可见性"：前置步骤没做完，后续工具**根本不在列表里**。

```
rollback        ── 始终可用（应急）
runUnitTests    ── 始终可用
runSmokeTests   ── 需要先跑过 runUnitTests
deployToStaging ── 需要 runUnitTests + runSmokeTests 都跑过
deployToProduction ── 需要先 deployToStaging
```

实测（`GET /playbook/release`）：

```json
{"unlockedTools":2,"lockedTools":3,
 "toolCalls":["runUnitTests","runSmokeTests","deployToStaging","deployToProduction"]}
```

日志印证每一步都过了"解锁门"：

```
ReleasePlaybookAgent - 手册初始状态：解锁 2 个，锁定 3 个
ConditionalTool - Tool 'runSmokeTests' is unlocked, executing
ConditionalTool - Tool 'deployToStaging' is unlocked, executing
ConditionalTool - Tool 'deployToProduction' is unlocked, executing
```

## 关键 API

| API | 作用 |
|---|---|
| `new PlaybookTool(name, description)` | 创建手册（本身是一个 `AgenticTool`，内部有自己的 LLM 循环） |
| `.withTools(tool...)` | **初始解锁**的工具 |
| `.withTool(tool).unlockedBy(prereq)` | 前置**一个**工具跑过才解锁 |
| `.unlockedByAll(a, b)` / `.unlockedByAny(a, b)` | 前置**全部** / **任一**跑过才解锁 |
| `.unlockedByArtifact(Class)` / `unlockedByArtifactMatching(predicate)` | 前置**产物**出现才解锁（数据驱动） |
| `.unlockedByBlackboard(Class)` / `unlockedByBlackboardMatching(predicate)` | 前置**黑板内容**满足条件才解锁 |
| `.unlockedWhen(UnlockCondition)` | 自定义条件；`new UnlockCondition.AfterTools("a","b")` 是内置实现 |
| `playbook.getUnlockedToolCount()` / `getLockedToolCount()` | 初始配置的解锁/锁定数量 |
| `playbook.call(input)` | 直接驱动手册（也可作为 `Tool` 交给外层 LLM） |

> **两个实测踩到的点**：
> 1. `.withTool(t).invoke()` **不是**"注册为解锁工具"——它仍算锁定（实测 `unlocked=0 locked=5`）。
>    初始解锁要用 `.withTools(t...)`。
> 2. **不要再套一层 LLM 工具循环**。`PlaybookTool` 是 `AgenticTool`，内部已经有一个 LLM 循环；
>    外层再包一层，模型只会看到一个"返回总结的黑盒"，反而会质疑"我没看到中间步骤"（第一版实测如此）。
>    直接 `playbook.call(...)` 更清晰。

## 接口

```bash
curl http://localhost:8940/playbook/release
```

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-playbook spring-boot:run
```

## 代码结构

- `ReleasePlaybookAgent.java` — 声明 5 个工具与依赖关系，直接驱动手册
- `PlaybookOutcome` — 结果（总结 + **实际调用顺序** + 解锁/锁定数量）
- `PlaybookController.java` — `GET /playbook/release`

## 要点

- **三种"按条件收敛工具集"的对比**（这是本模块最值得记的一张表）：

  | 模块 | 收敛依据 | 适合 |
  |---|---|---|
  | `embabel-state-machine` | 显式**状态枚举** | 状态少、转移明确（订单/审批） |
  | `embabel-tool-chaining` | 黑板上**出现了什么对象** | 数据驱动（查到订单→解锁订单操作） |
  | `embabel-playbook`（本模块） | **前置工具是否已执行** | 流程驱动（上线手册、检查清单） |

- **比提示词叮嘱可靠**：模型不会"跳过前置步骤"，因为跳不过去——那个工具不存在。
  这和 `embabel-secure-tools` 的"不给就不会被调用"是同一个思路，只是约束来自**流程**而非**权限**。
- **`unlockedByArtifact` 让流程与数据结合**：例如"拿到订单对象后解锁退款"，
  相当于把 tool-chaining 的机制接进手册。
- 手册内部的 LLM 循环会自己决定顺序与是否跳过，所以**不要指望它 100% 按你心里的顺序走**；
  要强确定性就用 `state-machine` 或在工具实现里自己断言前置条件。
