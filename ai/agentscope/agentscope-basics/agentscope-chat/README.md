# agentscope-chat — 最小聊天 Agent

## 演示内容

用 AgentScope Java 的 `HarnessAgent` 构建一个最小聊天 Agent，通过 DeepSeek（OpenAI 兼容接口）回答问题。

```bash
curl -G --data-urlencode "message=用一句话介绍 AgentScope" http://localhost:9100/ai/generate
```

实测回复：

```
AgentScope 是阿里巴巴开源的一个以开发者为中心的多智能体（Multi-Agent）应用开发框架，
让开发者能像搭积木一样轻松构建、排和运行由大模型驱动的多个智能体协作系统。
```

## 关键 API

| API | 作用 |
|---|---|
| `OpenAIChatModel.builder().apiKey().modelName().baseUrl().build()` | 构建 OpenAI 兼容模型（DeepSeek） |
| `HarnessAgent.builder().name().sysPrompt().model().workspace().build()` | 构建 Agent（在 ReActAgent 之上提供 workspace/memory/subagent/middleware） |
| `agent.call(new UserMessage(msg), RuntimeContext).block()` | 阻塞式调用，返回最终回复 |
| `RuntimeContext.builder().sessionId().userId().build()` | 运行时上下文（会话/用户隔离） |

## 运行

```bash
cd ai
mvn -pl :agentscope-chat spring-boot:run
```

> 需要 `OPENAI_API_KEY` 环境变量（DeepSeek key）。

## 技术栈

- Spring Boot **4.0.3**
- AgentScope Java **2.0.3**（BOM 管版本）
- `agentscope-spring-boot-starter` + `agentscope-openai-spring-boot-starter`（自动配置）
- `agentscope-harness`（HarnessAgent，starter 把 core 标为 provided 所以需显式加）
- Java 21

## 代码结构

- `ChatAgent.java` — `HarnessAgent` 的 lazy 构造 + 阻塞式调用
- `ChatController.java` — `GET /ai/generate`
- `AgentScopeApplication.java` — Spring Boot 入口

## 要点

- **HarnessAgent 是 AgentScope Java 的"工程化"层**：在 core 的 ReActAgent 之上加了 workspace、
  memory compaction、subagent、skill curator、plan mode 等能力。本模块只用到最基础的聊天功能，
  后续模块会逐步展示它的高级特性。
- **lazy 构造**：Agent 在第一次 `chat()` 时才创建（需要 API Key），这样单元测试不需要真实 Key。
- **与 Embabel 的区别**：Embabel 用 `@Agent` + `@Action` + `@AchievesGoal` 声明式定义 Agent，
  框架按目标类型选择 Agent；AgentScope 用 builder 手动构造，一个应用里可以有多个 Agent 实例，
  各自独立调用。