# AgentScope Java 教程

用 **Java 21 + Spring Boot 4.0.3 + AgentScope 2.0.3** 演示 AgentScope Java 的核心能力与独有特性。

> AgentScope 是阿里巴巴开源的 Agent 框架（Python 2.0.8 / Java 2.0.3）。
> Java 版（`io.agentscope`）在 Maven Central 上发布，核心 API 框架无关（core/harness 不依赖 Spring），
> 但提供了 Spring Boot 4 starter 用于自动配置。本教程用 Java 版，模型接入 DeepSeek（OpenAI 兼容接口）。

## 与 Embabel 的关系

本仓库的 `ai/embabel`（52 模块）用 Embabel 1.0.0 + Spring Boot 3.5 演示"类型化建模 + GOAP 规划器"范式。
AgentScope 是另一种范式：**ReAct 循环 + Middleware 链 + Permission 引擎 + Workspace/Sandbox**——
不靠规划器推导动作，而是让模型自主决策、用中间件做约束。

两者用不同的 Spring Boot 版本（Embabel 3.5 / AgentScope 4.0.3），互不影响。

## 分类与模块（规划中，逐步补充）

### ① [基础（agentscope-basics）](agentscope-basics/README.md)

| 模块 | 端口 | 主题 | 接口 |
|---|---|---|---|
| [agentscope-chat](agentscope-basics/agentscope-chat/README.md) | 9100 | 最小聊天 Agent（HarnessAgent + DeepSeek） | `GET /ai/generate` |

### ② 中间件（agentscope-middleware）★ — 待补

### ③ 工具与技能（agentscope-tools-skills）★ — 待补

### ④ 权限与 HITL（agentscope-permission）★ — 待补

### ⑤ 工作区与沙箱（agentscope-workspace）★ — 待补

### ⑥ 记忆与知识（agentscope-memory-rag） — 待补

### ⑦ 服务与渠道（agentscope-service）★ — 待补

### ⑧ 综合（agentscope-capstone） — 待补

## 运行

```bash
cd ai
mvn -pl :agentscope-chat spring-boot:run
# 或
java -jar agentscope/agentscope-basics/agentscope-chat/target/agentscope-chat-1.0.jar
```

> 需要 `OPENAI_API_KEY` 环境变量（DeepSeek key）。