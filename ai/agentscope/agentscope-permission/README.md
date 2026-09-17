# ④ 权限与 HITL ★（agentscope-permission）

## 这一章解决什么

AgentScope 的权限引擎——5 种模式 + 细粒度规则 + 人工确认 + 实时打断。
让 Agent 在"自主"和"安全"之间找到平衡。

## 模块清单

| 模块 | 端口 | 主题 | 接口 |
|---|---|---|---|
| [agentscope-permission-modes](agentscope-permission-modes/README.md) | 9115 | 权限模式（DEFAULT/ACCEPT_EDITS/EXPLORE/BYPASS/DONT_ASK） | `GET /permission/default` |
| [agentscope-permission-rules](agentscope-permission-rules/README.md) | 9116 | 权限规则（PermissionRule + PermissionEngine） | `GET /permission/rules` |
| [agentscope-hitl-confirm](agentscope-hitl-confirm/README.md) | 9117 | 人工确认（PermissionBehavior.ASK） | `GET /hitl/confirm` |
| [agentscope-interrupt](agentscope-interrupt/README.md) | 9118 | 实时打断（Agent.interrupt()） | `GET /interrupt/start` |

## 权限模式

| 模式 | 行为 |
|---|---|
| `DEFAULT` | 危险操作需确认 |
| `ACCEPT_EDITS` | 自动接受文件编辑 |
| `EXPLORE` | 只读模式 |
| `BYPASS` | 完全跳过权限检查 |
| `DONT_ASK` | 不询问，按规则自动决策 |

## 权限规则行为

| Behavior | 说明 |
|---|---|
| `ALLOW` | 允许执行 |
| `DENY` | 拒绝执行 |
| `ASK` | 需要用户确认（HITL） |
| `PASSTHROUGH` | 传递给下一条规则 |