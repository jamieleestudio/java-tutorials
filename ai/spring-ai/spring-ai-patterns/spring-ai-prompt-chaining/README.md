# spring-ai-prompt-chaining — Prompt Chaining + 关卡（多次 call 链式）

## 演示内容

Prompt Chaining + 关卡（多次 call 链式）。

`ash
curl -G --data-urlencode "message=你好" http://localhost:8016/ai/prompt-chain
`

> 需要 OPENAI_API_KEY 环境变量（DeepSeek key）。

## 运行

`ash
cd ai/spring-ai
mvn -pl :spring-ai-prompt-chaining spring-boot:run
`
