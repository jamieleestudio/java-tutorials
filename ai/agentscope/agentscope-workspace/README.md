# ⑤ 工作区与沙箱 ★（agentscope-workspace）

## 这一章解决什么

AgentScope 的工作区抽象——本地、Docker 沙箱、远程文件系统、快照恢复、计划模式。
让 Agent 的文件操作有安全边界。

## 模块清单

| 模块 | 端口 | 主题 | 接口 |
|---|---|---|---|
| [agentscope-local-workspace](agentscope-local-workspace/README.md) | 9119 | 本地工作区（LocalFilesystem + LocalFilesystemSpec） | `GET /workspace/local` |
| [agentscope-docker-sandbox](agentscope-docker-sandbox/README.md) | 9120 | Docker 沙箱（DockerFilesystemSpec） | `GET /sandbox/ask` |
| [agentscope-remote-filesystem](agentscope-remote-filesystem/README.md) | 9121 | 远程文件系统（RemoteFilesystem + InMemoryStore） | `GET /workspace/remote` |
| [agentscope-workspace-snapshot](agentscope-workspace-snapshot/README.md) | 9122 | 工作区快照（SandboxSnapshotSpec） | `GET /workspace/snapshot` |
| [agentscope-plan-mode](agentscope-plan-mode/README.md) | 9123 | 计划模式（enterPlanMode / exitPlanMode） | `GET /plan/ask` |

## 文件系统层次

```
AbstractFilesystem (接口)
├── LocalFilesystem          — 本地文件系统
│   └── LocalFilesystemWithShell  — 本地 + shell
├── RemoteFilesystem         — 远程存储（BaseStore）
└── SandboxBackedFilesystem  — 沙箱文件系统
    └── DockerSandbox         — Docker 容器
```

## 计划模式

```java
agent.enterPlanMode(ctx);   // 进入只读分析模式
agent.exitPlanMode(ctx);    // 退出，恢复执行
agent.isPlanModeActive(ctx); // 检查状态
```