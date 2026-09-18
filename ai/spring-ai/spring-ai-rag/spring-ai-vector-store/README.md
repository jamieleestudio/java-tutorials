# spring-ai-vector-store — 向量存储：SimpleVectorStore + 增删查

## 演示内容

向量存储：SimpleVectorStore + 增删查。

`ash
curl -G --data-urlencode "message=你好" http://localhost:8011/ai/vector
`

> 需要 OPENAI_API_KEY 环境变量（DeepSeek key）。

## 运行

`ash
cd ai/spring-ai
mvn -pl :spring-ai-vector-store spring-boot:run
`
