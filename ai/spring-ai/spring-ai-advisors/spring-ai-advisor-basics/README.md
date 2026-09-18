# spring-ai-advisor-basics — Advisor 基础：SimpleLoggerAdvisor + 自定义 BaseAdvisor

## 演示内容

Advisor 基础：SimpleLoggerAdvisor + 自定义 BaseAdvisor。

`ash
curl -G --data-urlencode "message=你好" http://localhost:8007/ai/advisor
`

> 需要 OPENAI_API_KEY 环境变量（DeepSeek key）。

## 运行

`ash
cd ai/spring-ai
mvn -pl :spring-ai-advisor-basics spring-boot:run
`
