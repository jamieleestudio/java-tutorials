# agentscope-rag — RAG 检索（Knowledge + RAGMode + GenericRAGHook）

## 演示内容

RAG 检索（Knowledge + RAGMode + GenericRAGHook）

```bash
curl -G --data-urlencode "message=你好" http://localhost:9126/rag/ask
```

> ⚠️ 当前 DeepSeek 余额不足（HTTP 402），LLM 调用会返回 500。
> 代码结构已就绪，充值后即可正常返回。

## 代码结构

- `RagAgent.java` — HarnessAgent + DeepSeek + GenericRAGHook
- `Controller` — `GET /rag/ask`

## 要点

- `Knowledge` 接口（addDocuments/retrieve），本例用内存关键词匹配实现。
- `GenericRAGHook` 在 PreCall 自动检索，把文档注入系统提示。
- 预先灌入知识库文档，生产可换向量库后端。

## 运行

```bash
cd ai
mvn -pl :agentscope-rag spring-boot:run
```

> 需要 `OPENAI_API_KEY` 环境变量（DeepSeek key）。
