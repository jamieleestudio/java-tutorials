# spring-ai-refinement — 自评迭代（生成→评估→改进）

## 演示内容

自评迭代（生成→评估→改进）。

`ash
curl -G --data-urlencode "message=你好" http://localhost:8020/ai/refinement
`

> 需要 OPENAI_API_KEY 环境变量（DeepSeek key）。

## 运行

`ash
cd ai/spring-ai
mvn -pl :spring-ai-refinement spring-boot:run
`
