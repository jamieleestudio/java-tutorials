# spring-ai-etl — ETL 管道：DocumentReader + TextSplitter + VectorStore

## 演示内容

ETL 管道：DocumentReader + TextSplitter + VectorStore。

`ash
curl -G --data-urlencode "message=你好" http://localhost:8014/ai/etl
`

> 需要 OPENAI_API_KEY 环境变量（DeepSeek key）。

## 运行

`ash
cd ai/spring-ai
mvn -pl :spring-ai-etl spring-boot:run
`
