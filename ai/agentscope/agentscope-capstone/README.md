# ⑧ 综合（agentscope-capstone）

## 这一章解决什么

整合前 7 个分类的 API，构建完整的 Agent 应用——从单一编码 Agent 到多 Agent 协作系统。

## 模块清单

| 模块 | 端口 | 主题 | 接口 |
|---|---|---|---|
| [agentscope-coding-agent](agentscope-coding-agent/README.md) | 9134 | 编码 Agent（整合 Shell+File+Compaction+Memory+Permission） | `GET /capstone/coding` |
| [agentscope-capstone-e2e](agentscope-capstone-e2e/README.md) | 9135 | 端到端多 Agent 系统（Subagent+Memory+Gateway） | `GET /capstone/e2e` |

## coding-agent 整合了什么

| 分类 | 用到的 API |
|---|---|
| basics | HarnessAgent builder + OpenAIChatModel |
| middleware | CompactionMiddleware（通过 .compaction()） |
| tools-skills | ShellExecuteTool + FilesystemTool |
| permission | PermissionMode.ACCEPT_EDITS |
| workspace | LocalFilesystemWithShell |
| memory | MemoryConfig |

## capstone-e2e 整合了什么

| 分类 | 用到的 API |
|---|---|
| basics | HarnessAgent builder |
| tools-skills | SubagentDeclaration（researcher + writer 子 Agent） |
| memory | MemoryConfig |
| service | REST controller（Gateway 入口） |