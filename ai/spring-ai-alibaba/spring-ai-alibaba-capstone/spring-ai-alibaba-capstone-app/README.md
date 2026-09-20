# spring-ai-alibaba-capstone-app — 端到端智能客服：Graph + RAG + 订单工具 + 退款 HITL

## 演示内容

端到端智能客服：Graph + RAG + 订单工具 + 退款 HITL。

```bash
curl "http://localhost:8530/"
```

> 启动时构建向量知识库需嵌入端点（DASHSCOPE_API_KEY 或 EMBEDDING_BASE_URL）；退款流程挂起于 human_confirm，需 confirm 恢复。

## 运行

```bash
cd ai/spring-ai-alibaba
mvn -pl :spring-ai-alibaba-capstone-app spring-boot:run
```