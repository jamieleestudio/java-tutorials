# spring-ai-parallelization — 并行 + 投票（Flux.merge 多 ChatClient）

## 演示内容

并行 + 投票（Flux.merge 多 ChatClient）。

`ash
curl -G --data-urlencode "message=你好" http://localhost:8018/ai/parallel
`

> 需要 OPENAI_API_KEY 环境变量（DeepSeek key）。

## 运行

`ash
cd ai/spring-ai
mvn -pl :spring-ai-parallelization spring-boot:run
`
