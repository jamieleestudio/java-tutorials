# spring-ai-alibaba-pattern-parallel — 并行模式：图扇出（addEdge(node, List)）+ 扇入汇合

## 演示内容

并行模式：图扇出（addEdge(node, List)）+ 扇入汇合。

```bash
curl "http://localhost:8521/"
```

> 三位评审并行，merge 隐式 barrier。

## 运行

```bash
cd ai/spring-ai-alibaba
mvn -pl :spring-ai-alibaba-pattern-parallel spring-boot:run
```