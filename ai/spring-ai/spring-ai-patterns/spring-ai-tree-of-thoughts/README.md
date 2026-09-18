# spring-ai-tree-of-thoughts — 思维树（多 temperature 分支 + 评分 + 深化）

## 演示内容

思维树（多 temperature 分支 + 评分 + 深化）。

`ash
curl -G --data-urlencode "message=你好" http://localhost:8022/ai/tot
`

> 需要 OPENAI_API_KEY 环境变量（DeepSeek key）。

## 运行

`ash
cd ai/spring-ai
mvn -pl :spring-ai-tree-of-thoughts spring-boot:run
`
