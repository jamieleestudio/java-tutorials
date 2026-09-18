# spring-ai-multi-agent-debate — 多 Agent 辩论（正反方 + 裁判）

## 演示内容

多 Agent 辩论（正反方 + 裁判）。

`ash
curl -G --data-urlencode "message=你好" http://localhost:8021/ai/debate
`

> 需要 OPENAI_API_KEY 环境变量（DeepSeek key）。

## 运行

`ash
cd ai/spring-ai
mvn -pl :spring-ai-multi-agent-debate spring-boot:run
`
