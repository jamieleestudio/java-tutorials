# spring-ai-structured-output — 结构化输出：entity() + BeanOutputConverter

## 演示内容

结构化输出：entity() + BeanOutputConverter。

`ash
curl -G --data-urlencode "message=你好" http://localhost:8002/ai/structured
`

> 需要 OPENAI_API_KEY 环境变量（DeepSeek key）。

## 运行

`ash
cd ai/spring-ai
mvn -pl :spring-ai-structured-output spring-boot:run
`
