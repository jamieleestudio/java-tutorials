# spring-ai-state-machine — 状态机（Advisor 按阶段切换提示 + 工具）

## 演示内容

状态机（Advisor 按阶段切换提示 + 工具）。

`ash
curl -G --data-urlencode "message=你好" http://localhost:8024/ai/state-machine
`

> 需要 OPENAI_API_KEY 环境变量（DeepSeek key）。

## 运行

`ash
cd ai/spring-ai
mvn -pl :spring-ai-state-machine spring-boot:run
`
