# ⑦ 编排模式 ★（spring-ai-alibaba-patterns）

## 这一章解决什么

与 ai/embabel、ai/agentscope、ai/spring-ai 的 patterns 1:1 对照，用 SAA Graph 范式实现：路由、并行、主管循环、模式综合。

## 模块清单

| 模块 | 端口 | 模式 | 接口 |
|---|---|---|---|
| [pattern-routing](./spring-ai-alibaba-pattern-routing/README.md) | 8520 | 路由分类 | `GET /patterns/routing/ask` |
| [pattern-parallel](./spring-ai-alibaba-pattern-parallel/README.md) | 8521 | 并行扇出/扇入 | `GET /patterns/parallel/ask` |
| [pattern-supervisor](./spring-ai-alibaba-pattern-supervisor/README.md) | 8522 | 主管编排循环 | `GET /patterns/supervisor/ask` |
| [pattern-capstone](./spring-ai-alibaba-pattern-capstone/README.md) | 8523 | 综合（路由+并行+迭代） | `GET /patterns/capstone/ask` |
