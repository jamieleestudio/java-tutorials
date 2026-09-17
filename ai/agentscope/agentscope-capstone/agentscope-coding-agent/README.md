# agentscope-coding-agent — 编码 Agent（Claude Code 式）

## 演示内容

编码 Agent（Claude Code 式）

```bash
curl -G --data-urlencode "message=你好" http://localhost:9134/coding-agent/ask
```

> ⚠️ 当前 DeepSeek 余额不足（HTTP 402），LLM 调用会返回 500。
> 代码结构已就绪，充值后即可正常返回。

## 代码结构

- `CodingCapstoneAgent.java` — 编码工具 + 计划模式 + 任务列表 + 权限 + 压缩
- `Controller` — `GET /coding-agent/ask`

## 要点

- 综合演示：ShellCommandTool + ReadFile/WriteFile + TodoTools + enablePlanMode + enableTaskList。
- 权限规则：删除操作 DENY、写操作 ASK，stopOnReject。
- 上下文压缩：CompactionConfig 长对话自动摘要。
- Claude Code 式工作方式：先规划再执行。

## 运行

```bash
cd ai
mvn -pl :agentscope-coding-agent spring-boot:run
```

> 需要 `OPENAI_API_KEY` 环境变量（DeepSeek key）。
