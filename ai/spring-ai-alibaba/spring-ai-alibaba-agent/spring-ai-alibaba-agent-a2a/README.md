# spring-ai-alibaba-agent-a2a — A2A 互操作（A2aRemoteAgent 远程 Agent-as-Node）

## 演示内容

A2A 互操作（A2aRemoteAgent 远程 Agent-as-Node）。

```bash
curl "http://localhost:8546/"
```

> 按 AgentCard 直连远程 A2A server（如 SAA Studio / a2a-nacos 注册的 Agent），未启动时返回连接提示。

## 运行

```bash
cd ai/spring-ai-alibaba
mvn -pl :spring-ai-alibaba-agent-a2a spring-boot:run
```