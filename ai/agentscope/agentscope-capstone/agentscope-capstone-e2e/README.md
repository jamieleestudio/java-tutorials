# agentscope-capstone-e2e — 端到端综合示例

## 演示内容

端到端综合示例

```bash
curl -G --data-urlencode "message=你好" http://localhost:9135/capstone/ask
```

> ⚠️ 当前 DeepSeek 余额不足（HTTP 402），LLM 调用会返回 500。
> 代码结构已就绪，充值后即可正常返回。

## 代码结构

- `CapstoneE2eAgent.java` — RAG + 记忆 + 工具 + 持久化 + 压缩
- `Controller` — `GET /capstone/ask`

## 要点

- 综合五大能力：GenericRAGHook 检索 + MemoryConfig 记忆 + Toolkit 工具 + JsonFileAgentStateStore 持久化 + CompactionConfig 压缩。
- 灌入知识库文档，回答时自动检索注入。
- 教程集大成模块，演示 AgentScope 完整工程化范式。

## 运行

```bash
cd ai
mvn -pl :agentscope-capstone-e2e spring-boot:run
```

> 需要 `OPENAI_API_KEY` 环境变量（DeepSeek key）。
