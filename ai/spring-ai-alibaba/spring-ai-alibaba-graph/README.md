# ② Graph 图编排 ★（spring-ai-alibaba-graph）

## 这一章解决什么

SAA 招牌能力：把大模型工作流画成图 —— StateGraph 声明节点/边，条件路由、checkpoint 恢复、人工挂起、子图组合、长期记忆与 checkpoint 落盘。

## 模块清单

| 模块 | 端口 | 主题 | 接口 |
|---|---|---|---|
| [graph-basics](./spring-ai-alibaba-graph-basics/README.md) | 8504 | StateGraph 基础 + Mermaid | `GET /graph/run` |
| [graph-conditional](./spring-ai-alibaba-graph-conditional/README.md) | 8505 | 条件边路由 | `GET /graph/route` |
| [graph-streaming](./spring-ai-alibaba-graph-streaming/README.md) | 8506 | 图级流式 | `GET /graph/stream` |
| [graph-checkpoint](./spring-ai-alibaba-graph-checkpoint/README.md) | 8507 | checkpoint + 状态历史 | `GET /graph/checkpoint/chat` |
| [graph-human-feedback](./spring-ai-alibaba-graph-human-feedback/README.md) | 8508 | HITL 挂起/恢复 | `POST /graph/hitl/start` |
| [graph-subgraph](./spring-ai-alibaba-graph-subgraph/README.md) | 8509 | 子图组合 | `GET /graph/subgraph/run` |
| [graph-tool-hitl](./spring-ai-alibaba-graph-tool-hitl/README.md) | 8547 | 工具级 HITL 审批 | `GET /agent/tool-hitl/start` |
| [graph-store](./spring-ai-alibaba-graph-store/README.md) | 8548 | 长期记忆 Store | `GET /graph/store/chat` |
| [graph-file-saver](./spring-ai-alibaba-graph-file-saver/README.md) | 8549 | checkpoint 落盘恢复 | `GET /graph/file-saver/chat` |
