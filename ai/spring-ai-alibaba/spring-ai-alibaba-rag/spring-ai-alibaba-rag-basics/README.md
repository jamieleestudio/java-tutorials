# spring-ai-alibaba-rag-basics — RAG 三段式：Retriever + Augmentation + Generation

## 演示内容

RAG 三段式：Retriever + Augmentation + Generation。

```bash
curl "http://localhost:8516/"
```

> 聊天走 DeepSeek、嵌入走 DashScope 兼容模式（embedding 段独立配置 base-url/api-key）。

## 运行

```bash
cd ai/spring-ai-alibaba
mvn -pl :spring-ai-alibaba-rag-basics spring-boot:run
```