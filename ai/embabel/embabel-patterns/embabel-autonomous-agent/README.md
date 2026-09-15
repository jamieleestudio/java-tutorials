# embabel-autonomous-agent — 自主 Agent（工具循环 + 环境反馈 + 错误恢复）

## 演示内容

Anthropic《Building Effective Agents》里的 **Agents**：LLM **在循环里使用工具**，
每步都从环境（工具返回）获取**真实反馈**判断进展；遇到错误能**自行恢复**；
并有**停止条件**防止失控。

本例演示三件事：
1. **工具循环 + 真实反馈**：模型可多次调用工具（计算器、知识库），结果回到模型再决策
2. **错误恢复**：知识库工具**首次调用故意返回 503 错误**，模型看到错误后改变策略重试
3. **停止条件**：工具循环有最大轮数上限（框架默认 20）

## 关键 API

| API | 作用 |
|---|---|
| `promptRunner.withTools(List.of(...))` | 挂多个工具，进入工具循环 |
| `Tool.create(name, desc, inputSchema, handler)` | 定义工具 |
| `Tool.Result.error(msg, cause)` | 工具返回**错误**给模型（触发恢复） |
| `Tool.Result.text(...)` | 工具返回正常结果（"ground truth"） |
| 工具循环 `max=N` | 停止条件（日志里可见 `starting tool loop [...] max=20`） |

## 接口

```bash
curl -G --data-urlencode "message=先查一下内部资料：引入 Agent 框架要注意什么？再算一下 1234 * 56 等于多少" \
     http://localhost:8920/autonomous/ask
```

实测日志（错误恢复过程）：

```
(solve) starting tool loop [calculator, knowledge_lookup] max=20
(solve) calling tool knowledge_lookup({"query":"引入 Agent 框架要注意什么"})
(solve) tool knowledge_lookup returned 知识库暂时不可用（错误码 503）...
(solve) calling tool calculator({"a":1234,"b":56,"op":"*"})      ← 换策略继续
```

最终回答里模型明确说明："该知识库服务首次调用返回了 503 错误，我换了问法重试后才拿到结果"。

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-autonomous-agent spring-boot:run
```

## 代码结构

- `AutonomousAgent.java` — `solve` 动作 + `calculator` / `knowledgeLookup` 两个工具（后者首次失败）
- `AutonomousController.java` — `GET /autonomous/ask`

## 要点

- **`Tool.Parameter.double(...)` 在 Java 里不可用**（`double` 是关键字）——
  用 `new Tool.Parameter("a", Tool.ParameterType.NUMBER, "描述")` 代替。
- 生产上自主 Agent 还需要：**人工检查点**（`embabel-hitl` 的 `WaitFor.confirmation`）、
  **护栏**（`embabel-guardrails`）、**沙箱化工具**（`embabel-file-tools`），
  以及成本/步数上限（`EarlyTerminationPolicy`、`Budget`）。
- 与 `embabel-tools` 的区别：tools 模块演示"怎么挂工具"；本模块演示"自主循环 + 错误恢复 + 停止条件"。
- 文章的三个原则同样适用：保持简单、让规划步骤透明（日志）、认真设计"Agent-计算机接口"（工具文档）。
