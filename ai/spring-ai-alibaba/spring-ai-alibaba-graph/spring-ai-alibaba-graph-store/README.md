# spring-ai-alibaba-graph-store — 长期记忆 Store（MemoryStore 命名空间跨 thread）

## 演示内容

长期记忆 Store（MemoryStore 命名空间跨 thread）。

```bash
curl "http://localhost:8548/"
```

> CompileConfig 挂 Store，节点经 config.store() 读写；内置 Memory/FileSystem/Redis/Mongo 实现。已运行验证。

## 运行

```bash
cd ai/spring-ai-alibaba
mvn -pl :spring-ai-alibaba-graph-store spring-boot:run
```