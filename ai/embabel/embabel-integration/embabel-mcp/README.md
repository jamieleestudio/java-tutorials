# embabel-mcp — MCP 集成

## 演示内容

通过 **MCP（Model Context Protocol）** 接入外部工具：本示例用 docker 拉起官方的
**filesystem MCP server**，把沙箱目录暴露给 Agent，模型即可列出/读取/写入文件。

## 关键 API / 配置

| 项 | 作用 |
|---|---|
| `spring.ai.mcp.client.stdio.connections.<name>.command/args` | 用 stdio 启动 MCP server（本示例是 `docker run -i ... mcp/filesystem`） |
| `embabel.agent.platform.tools.includes.<group>` | 把 MCP 工具按名字挂成**工具组**（`tools` 列表按后缀匹配） |
| `promptRunner.withToolGroup("files")` | 在动作里请求该工具组 |
| `McpToolGroup`（框架内部） | 惰性加载：首次使用才与 MCP server 握手 |

## 前置条件

本机能执行 `docker`（应用会用它拉起 MCP server），并已拉取镜像：

```bash
docker pull mcp/filesystem
```

沙箱目录由 `demo.mcp-root` 配置（默认 `${java.io.tmpdir}/embabel-mcp-sandbox`），启动时自动创建。

## 接口

```bash
curl -G --data-urlencode "message=列出沙箱目录里的文件，读取 readme.md 并用一句话总结" http://localhost:8909/mcp/ask
```

实测返回（节选）：模型先 `list_directory`，再 `read_text_file`，最后总结——全程由 MCP 工具完成。

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-mcp spring-boot:run
```

启动日志里可以看到：

```
StdioClientTransport - MCP server started
StdioClientTransport - STDERR Message received: Secure MCP Filesystem Server running on stdio
ToolGroupsConfiguration - Exposing tool group files
```

## 代码结构

- `application.yml` — MCP client（stdio→docker）+ 工具组 `files`
- `McpSandboxInitializer.java` — 准备沙箱目录与示例文件
- `McpAgent.java` — `withToolGroup("files")`
- `McpController.java` — `GET /mcp/ask`

## 要点

- **工具组名字与过滤**：`includes.files.tools` 里的名字按"后缀匹配"过滤 MCP server 暴露的工具；
  名字写错会导致该组为空（日志里 `Exposing tool group files` 后能看到实际加载的工具）。
- MCP server 是**按需启动**的（`docker run -i`），进程随应用生命周期存在；stdio 模式下不需要常驻容器。
- 安全：filesystem server 只允许访问传入的目录（`/projects`），示例把它绑定到本地沙箱目录。
- 除了 stdio，Spring AI 还支持 SSE / Streamable HTTP 连接远程 MCP server（`spring.ai.mcp.client.sse.connections.*`）。
- 其他 MCP server（github / brave-search / puppeteer 等）在 Embabel 里已有内置的工具组条件装配，
  只要连接名匹配（如 `github-mcp`）就会自动暴露。
