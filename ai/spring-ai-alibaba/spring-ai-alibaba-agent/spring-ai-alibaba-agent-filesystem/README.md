# spring-ai-alibaba-agent-filesystem — 内置文件工具套件（ReadFile/WriteFile/EditFile/Grep）

## 演示内容

内置文件工具套件（ReadFile/WriteFile/EditFile/Grep）。

```bash
curl "http://localhost:8540/"
```

> createXxxToolCallback(baseDir) 把操作锁定在工作目录。M1.1 的 GlobTool/ListFilesTool 生成 schema 有缺陷（DeepSeek 拒绝）暂未注册。已运行验证：真实读写文件。

## 运行

```bash
cd ai/spring-ai-alibaba
mvn -pl :spring-ai-alibaba-agent-filesystem spring-boot:run
```