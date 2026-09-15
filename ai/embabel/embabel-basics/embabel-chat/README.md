# embabel-chat — 最小聊天 Agent

## 演示内容

Embabel 的最小可运行形态：一个 `@Agent` 类，一个 `@Action` 方法，通过注入的 `Ai` 网关调用大模型。

## 关键 API

| API | 作用 |
|---|---|
| `@Agent(description=...)` | 声明这是一个 Agent（Spring 构造型注解，会被扫描注册）；`@Agent` 是 `@Component` |
| `@Action(description=...)` | 标记可执行动作；方法参数是输入类型，返回类型是产出 |
| `@AchievesGoal(description=...)` | 声明该动作完成即达成目标 |
| `Ai.withDefaultLlm()` | 取默认 LLM 的 `PromptRunner` |
| `AgentInvocation.create(platform, T.class).invoke(...)` | 以强类型方式运行 Agent 并按目标类型取回结果 |

## 接口

```bash
curl -G --data-urlencode "message=讲个笑话" http://localhost:8889/ai/generate
```

`message` 缺省为「讲个笑话」。

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-chat spring-boot:run
```

## 代码结构

- `EmbabelApplication.java` — Spring Boot 入口
- `ChatAgent.java` — `@Agent` + 单个 `@Action chat(UserInput, Ai)`
- `ChatReply.java` — 目标输出类型（record）
- `ChatController.java` — `GET /ai/generate`，用 `AgentInvocation` 运行 Agent
- `src/test/.../ChatAgentTest.java` — Mockito 单测，无需 API Key

## 要点

- `@Action` 的输出类型即"目标"，所以调用方只需要声明想要 `ChatReply`，无需关心内部动作。
- 单测直接 mock `Ai` 与 `PromptRunner`，可脱离网络验证动作逻辑。
