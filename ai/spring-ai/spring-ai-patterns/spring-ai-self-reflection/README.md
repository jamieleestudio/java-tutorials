# spring-ai-self-reflection — 自省 + 渐进式工具（自定义 Advisor）

## 演示内容

自省 + 渐进式工具（自定义 Advisor）。

`ash
curl -G --data-urlencode "message=你好" http://localhost:8023/ai/self-reflection
`

> 需要 OPENAI_API_KEY 环境变量（DeepSeek key）。

## 运行

`ash
cd ai/spring-ai
mvn -pl :spring-ai-self-reflection spring-boot:run
`
