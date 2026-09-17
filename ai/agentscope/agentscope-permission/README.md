# ④ 权限与 HITL ★（agentscope-permission）

## 模块清单

| 模块 | 端口 | 主题 |
|---|---|---|
| [agentscope-permission-modes](../agentscope-permission/agentscope-permission-modes/README.md) | 9115 | 权限模式（bypass/confirm/strict） |
| [agentscope-permission-rules](../agentscope-permission/agentscope-permission-rules/README.md) | 9116 | 权限规则（PermissionRule + PermissionEngine） |
| [agentscope-hitl-confirm](../agentscope-permission/agentscope-hitl-confirm/README.md) | 9117 | 人工确认（RequireUserConfirmEvent） |
| [agentscope-interrupt](../agentscope-permission/agentscope-interrupt/README.md) | 9118 | 实时打断（Agent.interrupt） |
