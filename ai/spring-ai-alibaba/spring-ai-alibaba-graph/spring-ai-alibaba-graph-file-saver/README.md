# spring-ai-alibaba-graph-file-saver — checkpoint 落盘（FileSystemSaver 持久化与恢复）

## 演示内容

checkpoint 落盘（FileSystemSaver 持久化与恢复）。

```bash
curl "http://localhost:8549/"
```

> 每个 checkpoint 序列化为 thread-*.saver 文件；每次请求新建图实例仍能恢复历史（模拟重启）。生产换 Mysql/Redis/Postgres/MongoSaver。已运行验证。

## 运行

```bash
cd ai/spring-ai-alibaba
mvn -pl :spring-ai-alibaba-graph-file-saver spring-boot:run
```