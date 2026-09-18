# agentscope-mcp — MCP 客户端（McpClientBuilder SSE/stdio/HTTP）

## 演示内容

MCP 客户端（McpClientBuilder SSE/stdio/HTTP）

```bash
curl -G --data-urlencode "message=你好" http://localhost:9110/mcp/ask
```

> ⚠️ 当前 DeepSeek 余额不足（HTTP 402），LLM 调用会返回 500。
> 代码结构已就绪，充值后即可正常返回。

## 代码结构

- `McpAgent.java` — HarnessAgent + DeepSeek + McpClientBuilder
- `Controller` — `GET /mcp/ask`

## 要点

- `McpClientBuilder.create(name).sseTransport(url).buildSync()` 构建 SSE MCP 客户端。
- `Toolkit.registerMcpClient(wrapper)` 把 MCP 工具注册进 Toolkit。
- 支持 stdio / SSE / streamableHttp 三种传输。运行需启动外部 MCP Server。

## 运行

```bash
cd ai
mvn -pl :agentscope-mcp spring-boot:run
```

> 需要 `OPENAI_API_KEY` 环境变量（DeepSeek key）。
