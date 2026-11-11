# spring-ai-tool-limits — 工具调用上限（spring.ai.tools.limits）

## 演示内容

Spring AI 2.0.1 新特性：全局可配置工具调用上限，防止失控的工具循环。

- `max-calls-per-tool-default: 2` + `max-total-tool-calls: 8`（application.yml）
- `on-limit-exceeded: RETURN_ERROR_RESPONSE`：超限后把错误作为工具结果回给模型，优雅降级
- 另一可选行为 `THROW`：抛 `ToolCallLimitExceededException`（含 toolName / limit / 部分结果，`buildGeneration()` 可取兜底 Generation）

```bash
cd ai/spring-ai/spring-ai-tools/spring-ai-tool-limits
# 需要环境变量 OPENAI_API_KEY（DeepSeek 兼容端点）
mvn spring-boot:run
```

> 端口 8027。接口：
> - `GET /ai/tools/limits` — 5 只股票查询请求，触发每工具 2 次上限
> - `GET /ai/tools/limits/noop` — 对照，不触发限制
>
> 其余说明见分类 README 与组 README。
