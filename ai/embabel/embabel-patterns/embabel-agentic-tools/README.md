# embabel-agentic-tools — Agent 自省工具

## 演示内容

让模型能查看**自己当前的运行状态**——框架内置两组开箱即用的自省工具：

- `BlackboardTools`：查看当前**黑板**（共享上下文）里的对象
- `AgentProcessTools`：查看当前**进程**的状态、目标、耗时、成本、token 用量、历史动作

## 关键 API

| API | 作用 |
|---|---|
| `new BlackboardTools().create()` | 返回"黑板查看"工具（渐进式工具门面） |
| `new AgentProcessTools().create()` | 返回"进程自省"工具 |
| `promptRunner.withTools(List.of(...))` | 挂到本次调用 |

> 这两组工具都**无需参数**：它们从当前 `AgentProcess`（线程本地）取上下文。
> 另外 `GoalTool` / `AgentTool` 可把"某个目标 / 另一个 Agent"包装成工具（需注入 `Autonomy`），
> 见 README 末尾说明。

## 接口

```bash
curl -G --data-urlencode "message=先查看你当前的黑板与进程状态，再回答：什么是类型化建模？" \
     http://localhost:8928/agentic-tools/ask
```

实测（模型自省后回答）：

```
黑板内容：共 1 个对象
- [0] UserInput：本轮的提问
进程状态：
| 进程 ID  | quizzical_pascal |
| 状态     | RUNNING          |
| 运行时间 | 2.1 s            |
| 目标     | com.third.li.IntrospectiveAgent.answer |
成本：本次 $0.0016 / 上限 $2.0000
Token：4,439 / 上限 1,000,000
动作：0 / 上限 50
模型：deepseek-flash（OpenAI）
```

日志：`(answer) starting tool loop [agent_process, blackboard] max=20`

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-agentic-tools spring-boot:run
```

## 代码结构

- `IntrospectiveAgent.java` — 挂两组自省工具，要求模型"先自省再回答"
- `AgenticToolsController.java` — `GET /agentic-tools/ask`

## 要点

- **为什么有用**：Agent 失败最常见的原因是"信息在上下文里但模型没意识到"。
  给它自省工具，它会主动确认现状，而不是凭空猜。
- 自省工具返回的是**渐进式工具**（门面 + 按需展开），因此不会一上来就塞满提示词
  （机制见 `embabel-tools-advanced`）。
- `GoalTool` / `AgentTool` 的构造需要 `Autonomy`、`Goal`/`Agent` 实例与 `TextCommunicator`，
  适合"让 Agent 自主调度其它目标/Agent"的场景；进程内委派的更简单做法见 `embabel-subagent`。
