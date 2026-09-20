# spring-ai-alibaba-graph-streaming — 图级流式：compiledGraph.stream() 逐节点事件

## 演示内容

图级流式：compiledGraph.stream() 逐节点事件。

```bash
curl "http://localhost:8506/"
```

> M1.1 中普通节点不支持 token 级 AsyncGenerator 直通（1.x 模式），本模块演示节点级流式；token 级用 ChatModel.stream。已运行验证。

## 运行

```bash
cd ai/spring-ai-alibaba
mvn -pl :spring-ai-alibaba-graph-streaming spring-boot:run
```