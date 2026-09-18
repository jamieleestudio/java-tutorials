# spring-ai-embedding — 向量嵌入：EmbeddingModel + Document

## 演示内容

向量嵌入：EmbeddingModel + Document。

`ash
curl -G --data-urlencode "message=你好" http://localhost:8010/ai/embedding
`

> 需要 OPENAI_API_KEY 环境变量（DeepSeek key）。

## 运行

`ash
cd ai/spring-ai
mvn -pl :spring-ai-embedding spring-boot:run
`
