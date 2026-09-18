# spring-ai-orchestrator-workers — 编排者-工人（主 Agent 用 @Tool 委派）

## 演示内容

编排者-工人（主 Agent 用 @Tool 委派）。

`ash
curl -G --data-urlencode "message=你好" http://localhost:8019/ai/orchestrator
`

> 需要 OPENAI_API_KEY 环境变量（DeepSeek key）。

## 运行

`ash
cd ai/spring-ai
mvn -pl :spring-ai-orchestrator-workers spring-boot:run
`
