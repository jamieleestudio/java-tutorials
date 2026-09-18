# ⑤ 多模态与 ETL（spring-ai-multimodal-etl）

## 这一章解决什么

图片输入 + 文档入库管道 + 可观测性。

## 模块清单

| 模块 | 端口 | 主题 | 接口 |
|---|---|---|---|
| [spring-ai-multimodal](./spring-ai-multimodal/README.md) | 8013 | 多模态 | `GET /ai/multimodal` |
| [spring-ai-etl](./spring-ai-etl/README.md) | 8014 | ETL 管道 | `GET /ai/etl` |
| [spring-ai-observability](./spring-ai-observability/README.md) | 8015 | 可观测性 | `GET /ai/observability` |
