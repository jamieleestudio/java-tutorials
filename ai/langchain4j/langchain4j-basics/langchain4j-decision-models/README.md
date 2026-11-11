# langchain4j-decision-models — 决策模型（DecisionModel + DecisionServices + 路由）

## 演示内容

LangChain4j 1.21+ 新特性（`@Experimental`）：决策模型用「问题 + 类型化答案 + 概率」代替文本生成，适合分类/路由/门控/打分。

- `ChatModelDecisionModel`：用普通 ChatModel 实现 `DecisionModel` 单方法契约（DeepSeek 等兼容端点没有专用决策端点，自己适配）
- `DecisionServices.create` + `@Decide`：像 AI Services 一样用接口声明决策问题，返回 boolean/枚举
- `DecisionModelChatModelRouter`（1.22+）：用决策模型把请求路由到 simple/complex 两类模型

```bash
cd ai/langchain4j/langchain4j-basics/langchain4j-decision-models
# 需要环境变量 DEEPSEEK_API_KEY（DeepSeek）
mvn spring-boot:run
```

> 端口 8701。接口：
> - `GET /decisions/complaint` — @Decide 布尔决策（yes/no 概率）
> - `GET /decisions/team` — @Decide 枚举决策
> - `GET /decisions/route` — 决策模型路由（simple/complex）
>
> 其余说明见分类 README 与组 README。
