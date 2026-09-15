# embabel-subagent — 子 Agent 委派（handoff）

## 演示内容

分层 Agent 模式：**调度 Agent** 把"翻译"这类专门任务委派给**专家 Agent**。
子 Agent 被包装成工具挂到当前 PromptRunner 上，由模型决定何时调用。

## 关键 API

| API | 作用 |
|---|---|
| `Subagent.ofClass(TranslatorAgent.class)` | 从 `@Agent` 类创建子 Agent 工具 |
| `.consuming(TranslationRequest.class)` | 声明子 Agent 的输入类型（用于生成工具 JSON Schema） |
| `promptRunner.withTool(tool)` | 把子 Agent 挂到本次调用 |
| `Subagent.byName("...")` / `ofInstance(...)` | 其他引用方式（按名解析 / 已解析实例） |

## 接口

```bash
curl -G --data-urlencode "message=把这句话翻译成日语：今天天气真好，我们去公园吧。" http://localhost:8893/subagent/translate
```

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-subagent spring-boot:run
```

## 代码结构

- `TranslatorAgent.java` — 专家 Agent，`TranslationRequest -> Translation`
- `CoordinatorAgent.java` — 调度 Agent，`Subagent.ofClass(TranslatorAgent.class).consuming(...)`
- `SubagentController.java` — `GET /subagent/translate`，目标为 `ChatReply`

## 要点

- 子 Agent 运行在**独立的子流程**中，并共享父流程的黑板上下文。
- 同样的委派还能用 `RunSubagent.instance(agent, Type.class)` 在动作里直接调用。
- 与工具的区别：子 Agent 自带规划能力，可以是一个多步 Agent。
