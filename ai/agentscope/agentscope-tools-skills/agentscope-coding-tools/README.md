# agentscope-coding-tools — 编码工具（ShellCommandTool + ReadFile/WriteFile + TodoTools）

## 演示内容

编码工具（ShellCommandTool + ReadFile/WriteFile + TodoTools）

```bash
curl -G --data-urlencode "message=你好" http://localhost:9109/coding/ask
```

> ⚠️ 当前 DeepSeek 余额不足（HTTP 402），LLM 调用会返回 500。
> 代码结构已就绪，充值后即可正常返回。

## 代码结构

- `CodingAgent.java` — HarnessAgent + DeepSeek + 编码工具集
- `CodingAgentController` — `GET /coding/ask`

## 要点

- 把 `ShellCommandTool`（带命令白名单）、`ReadFileTool`、`WriteFileTool`、`TodoTools` 注册到同一个 `Toolkit`。
- `ShellCommandTool` 实现了 `AgentTool` 接口，用 `registerAgentTool` 注册；其余三个用 `@Tool` 注解，用 `registerTool` 注册。
- 文件工具通过构造参数 `baseDir` 限制读写范围在工作区目录内。

## 运行

```bash
cd ai
mvn -pl :agentscope-coding-tools spring-boot:run
```

> 需要 `OPENAI_API_KEY` 环境变量（DeepSeek key）。
