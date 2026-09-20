# spring-ai-alibaba-agent-todos — 任务清单（WriteTodosTool + TodoListInterceptor）

## 演示内容

任务清单（WriteTodosTool + TodoListInterceptor）。

```bash
curl "http://localhost:8542/"
```

> 拦截器注入 write_todos 工具并在每次模型调用前把清单注入上下文（类 Claude Code TodoList）。已运行验证。

## 运行

```bash
cd ai/spring-ai-alibaba
mvn -pl :spring-ai-alibaba-agent-todos spring-boot:run
```