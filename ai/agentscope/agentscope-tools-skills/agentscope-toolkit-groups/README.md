# agentscope-toolkit-groups — 工具分组（ToolGroup + ToolGroupManager）

## 演示内容

工具分组（ToolGroup + ToolGroupManager）

```bash
curl -G --data-urlencode "message=你好" http://localhost:9108/toolgroup/ask
```

> ⚠️ 当前 DeepSeek 余额不足（HTTP 402），LLM 调用会返回 500。
> 代码结构已就绪，充值后即可正常返回。

## 代码结构

- `ToolGroupAgent.java` — HarnessAgent + DeepSeek + ToolGroup
- `Controller` — `GET /toolgroup/ask`

## 要点

- 用 `ToolGroup.builder()` 创建工具组，`Toolkit.registerToolGroup` 注册。
- `Toolkit.setActiveGroups(...)` 切换激活组，只有激活组内工具暴露给模型。
- 两组：`time-group`（默认激活）、`order-group`（按需切换）。

## 运行

```bash
cd ai
mvn -pl :agentscope-toolkit-groups spring-boot:run
```

> 需要 `OPENAI_API_KEY` 环境变量（DeepSeek key）。
