# ③ 工具与技能 ★（agentscope-tools-skills）

## 这一章解决什么

AgentScope 的工具生态：内置编码工具、MCP 客户端、技能系统、子代理、后台任务——
从"Agent 能调工具"到"Agent 能管理自己的能力"。

## 模块清单

| 模块 | 端口 | 主题 | 接口 |
|---|---|---|---|
| [agentscope-toolkit-groups](agentscope-toolkit-groups/README.md) | 9108 | 工具分组（ToolGroup + group registration） | `GET /toolgroup/ask` |
| [agentscope-coding-tools](agentscope-coding-tools/README.md) | 9109 | 编码工具（ShellExecuteTool + FilesystemTool） | `GET /coding/ask` |
| [agentscope-mcp](agentscope-mcp/README.md) | 9110 | MCP 客户端（McpClientBuilder SSE/stdio/HTTP） | `GET /mcp/ask` |
| [agentscope-skills](agentscope-skills/README.md) | 9111 | 技能系统（SkillBox + AgentSkill + WorkspaceSkillRepository） | `GET /skills/ask` |
| [agentscope-skill-curator](agentscope-skill-curator/README.md) | 9112 | 技能策展（SkillCurator + SkillPromoter） | `GET /curator/ask` |
| [agentscope-subagent](agentscope-subagent/README.md) | 9113 | 子代理（SubagentDeclaration + Agent-as-Tool） | `GET /subagent/ask` |
| [agentscope-background-tasks](agentscope-background-tasks/README.md) | 9114 | 后台任务（TaskTool + TaskRepository） | `GET /background/ask` |

## 核心概念

| 能力 | 关键类 | 说明 |
|---|---|---|
| 工具分组 | `Toolkit.ToolRegistration.group()` | 把工具划入命名组 |
| 编码工具 | `ShellExecuteTool` + `FilesystemTool` | 内置 shell + 文件操作 |
| MCP | `McpClientBuilder` | 接入外部 MCP server |
| 技能 | `AgentSkill` + `SkillBox` | 文本注入提示（非代码） |
| 技能策展 | `SkillCurator` | 自动评估使用并晋升 |
| 子代理 | `SubagentDeclaration` | 把 Agent 包装成工具 |
| 后台任务 | `TaskTool` + `TaskRepository` | 长任务异步执行 |