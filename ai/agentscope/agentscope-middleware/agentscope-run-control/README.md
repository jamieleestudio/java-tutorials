# agentscope-run-control — 运行级控制（AgentRun / RunControl）

## 演示内容

AgentScope 2.0.4 新特性：

- `prepareCall / prepareRun` 返回 `AgentRun`：独立 runId、`status()` / `termination()`、`cancel()` 取消单次运行、`interrupt()` 中断插话（RuntimeContext 显式传入）
- `@Tool(returnDirect = true)`：工具结果直接短路推理循环（`/runcontrol/direct-tool`）
- `fallbackModel + failoverListener`：主模型失败自动回退并回调监听器

```bash
cd ai/agentscope/agentscope-middleware/agentscope-run-control
# 需要环境变量 OPENAI_API_KEY（DeepSeek 兼容端点）
mvn spring-boot:run
```

> 端口 9136。接口：`POST /runcontrol/start` → `GET /runcontrol/status|result` → `POST /runcontrol/cancel|interrupt`。
>
> 其余说明见分类 README 与组 README。
