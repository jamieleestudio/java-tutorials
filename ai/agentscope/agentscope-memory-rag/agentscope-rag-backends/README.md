# agentscope-rag-backends — RAG 后端（extensions-rag-simple）

## 演示内容

RAG 后端（extensions-rag-simple）

```bash
curl -G --data-urlencode "message=你好" http://localhost:9127/rag/backends
```

> ⚠️ 当前 DeepSeek 余额不足（HTTP 402），LLM 调用会返回 500。
> 代码结构已就绪，充值后即可正常返回。

## 代码结构

- `RagBackendAgent.java` — HarnessAgent + DeepSeek + KnowledgeRetrievalTools
- `Controller` — `GET /rag/backends`

## 要点

- Agentic RAG：`KnowledgeRetrievalTools` 把检索包装成 @Tool，模型按需调用。
- 与 Generic RAG 区别：不是每次自动检索，而是模型决定何时检索。
- 本例用内存后端演示，可换 rag-simple / 向量库后端。

## 运行

```bash
cd ai
mvn -pl :agentscope-rag-backends spring-boot:run
```

> 需要 `OPENAI_API_KEY` 环境变量（DeepSeek key）。
