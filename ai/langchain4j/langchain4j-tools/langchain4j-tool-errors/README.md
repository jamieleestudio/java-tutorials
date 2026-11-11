# langchain4j-tool-errors — 工具错误处理（LlmVisibleToolExecutionException）

## 演示内容

LangChain4j 1.21+ 新特性：让**每个异常自行决定向 LLM 暴露什么**。

- `LlmVisibleToolExecutionException`：预期内业务失败，消息原样给到 LLM，模型据此向用户解释/给建议
- 普通 `RuntimeException`：内部细节（堆栈/内网地址）默认不进 LLM 上下文，只给通用失败提示

对照旧版全局开关 `ToolExecutionExceptionProcessor`，粒度从"全局"细化到"每个异常"。

```bash
cd ai/langchain4j/langchain4j-tools/langchain4j-tool-errors
# 需要环境变量 DEEPSEEK_API_KEY（DeepSeek）
mvn spring-boot:run
```

> 端口 8702。接口：
> - `GET /tools/errors/visible` — LLM 可见错误
> - `GET /tools/errors/hidden` — LLM 不可见错误
>
> 其余说明见分类 README 与组 README。
