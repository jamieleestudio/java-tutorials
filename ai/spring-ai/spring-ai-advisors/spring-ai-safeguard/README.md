# spring-ai-safeguard — 安全护栏：SafeGuardAdvisor 关键词拦截

## 演示内容

安全护栏：SafeGuardAdvisor 关键词拦截。

`ash
curl -G --data-urlencode "message=你好" http://localhost:8008/ai/safeguard
`

> 需要 OPENAI_API_KEY 环境变量（DeepSeek key）。

## 运行

`ash
cd ai/spring-ai
mvn -pl :spring-ai-safeguard spring-boot:run
`
