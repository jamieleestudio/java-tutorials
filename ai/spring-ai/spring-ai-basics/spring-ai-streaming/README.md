# spring-ai-streaming — 流式输出：chatModel.stream + Flux + SSE

## 演示内容

流式输出：chatModel.stream + Flux + SSE。

`ash
curl -G --data-urlencode "message=你好" http://localhost:8001/ai/streaming
`

> 需要 OPENAI_API_KEY 环境变量（DeepSeek key）。

## 运行

`ash
cd ai/spring-ai
mvn -pl :spring-ai-streaming spring-boot:run
`
