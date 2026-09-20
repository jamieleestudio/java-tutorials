# spring-ai-alibaba-embedding — 向量嵌入：EmbeddingModel 单文本/Document/批量

## 演示内容

向量嵌入：EmbeddingModel 单文本/Document/批量。

```bash
curl "http://localhost:8514/"
```

> 默认 DashScope 兼容模式 text-embedding-v3，需 DASHSCOPE_API_KEY；可用 EMBEDDING_BASE_URL/EMBEDDING_MODEL 换任意 OpenAI 兼容嵌入服务。

## 运行

```bash
cd ai/spring-ai-alibaba
mvn -pl :spring-ai-alibaba-embedding spring-boot:run
```