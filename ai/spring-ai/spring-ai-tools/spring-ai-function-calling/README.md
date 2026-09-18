# spring-ai-function-calling — 函数调用：@Tool + ChatClient.tools()

## 演示内容

函数调用：@Tool + ChatClient.tools()。

`ash
curl -G --data-urlencode "message=你好" http://localhost:8004/ai/function
`

> 需要 OPENAI_API_KEY 环境变量（DeepSeek key）。

## 运行

`ash
cd ai/spring-ai
mvn -pl :spring-ai-function-calling spring-boot:run
`
