# spring-ai-alibaba-agent-shell — Shell 命令执行（ShellTool2 + ShellToolAgentHook）

## 演示内容

Shell 命令执行（ShellTool2 + ShellToolAgentHook）。

```bash
curl "http://localhost:8541/"
```

> ShellToolAgentHook 实现 ToolInjection 自动注入工具。生产请配合沙箱。已运行验证：真实执行 PowerShell。

## 运行

```bash
cd ai/spring-ai-alibaba
mvn -pl :spring-ai-alibaba-agent-shell spring-boot:run
```