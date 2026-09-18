# spring-ai-chat-memory-advisor — 对话记忆：MessageChatMemoryAdvisor + MessageWindowChatMemory

## 演示内容

对话记忆：MessageChatMemoryAdvisor + MessageWindowChatMemory。

`ash
curl -G --data-urlencode "message=你好" http://localhost:8009/ai/memory
`

> 需要 OPENAI_API_KEY 环境变量（DeepSeek key）。

## 运行

`ash
cd ai/spring-ai
mvn -pl :spring-ai-chat-memory-advisor spring-boot:run
`
