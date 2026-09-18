# spring-ai-chat — 基础聊天：ChatClient + ChatModel（DeepSeek）

## 演示内容

基础聊天：ChatClient + ChatModel（DeepSeek）。

`ash
curl -G --data-urlencode "message=你好" http://localhost:8000/ai/chat
`

> 需要 OPENAI_API_KEY 环境变量（DeepSeek key）。

## 运行

`ash
cd ai/spring-ai
mvn -pl :spring-ai-chat spring-boot:run
`
