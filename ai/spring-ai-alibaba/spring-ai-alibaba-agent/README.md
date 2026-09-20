# ③ Agent Framework ★（spring-ai-alibaba-agent）

## 这一章解决什么

ReactAgent 智能体：Builder 装配 + ReAct 循环，配 Hook（限流/摘要/PII）与 Interceptor（子 Agent 委派等），Agent 内部就是一张图。8540-8546 补齐 SAA 的 Coding Agent 能力（文件/Shell/Todos）与工程化能力（技能/调度/A2A）。

## 模块清单

| 模块 | 端口 | 主题 | 接口 |
|---|---|---|---|
| [react-agent](./spring-ai-alibaba-react-agent/README.md) | 8510 | ReactAgent + Hook | `GET /agent/ask` |
| [agent-tools](./spring-ai-alibaba-agent-tools/README.md) | 8511 | @Tool 工具调用 | `GET /agent/tools/ask` |
| [agent-memory](./spring-ai-alibaba-agent-memory/README.md) | 8512 | 会话记忆 | `GET /agent/memory/chat` |
| [agent-multi-agent](./spring-ai-alibaba-agent-multi-agent/README.md) | 8513 | 多 Agent 委派 | `GET /agent/multi/ask` |
| [agent-filesystem](./spring-ai-alibaba-agent-filesystem/README.md) | 8540 | 内置文件工具套件 | `GET /agent/fs/ask` |
| [agent-shell](./spring-ai-alibaba-agent-shell/README.md) | 8541 | Shell 命令执行 | `GET /agent/shell/ask` |
| [agent-todos](./spring-ai-alibaba-agent-todos/README.md) | 8542 | 任务清单 | `GET /agent/todos/ask` |
| [agent-hooks](./spring-ai-alibaba-agent-hooks/README.md) | 8543 | 摘要/PII/限流 Hook | `GET /agent/hooks/summary` |
| [agent-skills](./spring-ai-alibaba-agent-skills/README.md) | 8544 | 技能体系（SKILL.md） | `GET /agent/skills/ask` |
| [agent-scheduling](./spring-ai-alibaba-agent-scheduling/README.md) | 8545 | 定时调度 | `GET /agent/schedule/status` |
| [agent-a2a](./spring-ai-alibaba-agent-a2a/README.md) | 8546 | A2A 远程互操作 | `GET /agent/a2a/ask` |
