# spring-ai-alibaba-multi-model — 多模型接入：DeepSeek + 通义千问（DashScope 兼容模式）

## 演示内容

多模型接入：DeepSeek + 通义千问（DashScope 兼容模式）。

```bash
curl "http://localhost:8503/"
```

> qwen 需要 DASHSCOPE_API_KEY，未配置时返回友好提示。注意：Qwen 模型刻意不注册为 Bean，避免触发自动配置的 ConditionalOnMissingBean 回退。

## 运行

```bash
cd ai/spring-ai-alibaba
mvn -pl :spring-ai-alibaba-multi-model spring-boot:run
```