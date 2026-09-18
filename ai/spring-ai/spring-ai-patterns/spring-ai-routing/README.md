# spring-ai-routing — 路由分类（结构化输出 → 分发专用 Agent）

## 演示内容

路由分类（结构化输出 → 分发专用 Agent）。

`ash
curl -G --data-urlencode "message=你好" http://localhost:8017/ai/routing
`

> 需要 OPENAI_API_KEY 环境变量（DeepSeek key）。

## 运行

`ash
cd ai/spring-ai
mvn -pl :spring-ai-routing spring-boot:run
`
