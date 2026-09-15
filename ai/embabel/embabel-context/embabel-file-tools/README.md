# embabel-file-tools — 文件工具（沙箱）

## 演示内容

给 Agent 挂一套**开箱即用的文件工具**，让它列出、读取、查找、创建、写入文件——
所有路径都被限制在指定的**沙箱目录**内。

## 关键 API

| API | 作用 |
|---|---|
| `FileTools.readOnly(root)` | 只读工具集（推荐默认） |
| `FileTools.readWrite(root)` | 可写工具集（本示例使用） |
| `Tool.fromInstance(tools)` | 把工具对象的 `@LlmTool` 方法转成 `Tool` 列表 |
| `promptRunner.withTools(...)` | 挂到本次 LLM 调用 |

工具方法（均带 `@LlmTool`）：`listFiles`、`readFile`、`findFiles`、`fileSize`、`fileCount`、
`createFile`、`writeFile`、`editFile`、`appendFile`、`createDirectory`、`delete`。

## 接口

```bash
curl -G --data-urlencode "message=列出沙箱目录里的文件，读取 notes.md 并用一句话总结它的内容" \
     http://localhost:8902/files/ask
```

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-file-tools spring-boot:run
```

沙箱目录由 `demo.sandbox-dir` 配置（默认 `${java.io.tmpdir}/embabel-file-tools-sandbox`），
启动时会自动创建并写入一个示例文件 `notes.md`。

## 代码结构

- `SandboxInitializer.java` — 启动时准备沙箱目录与示例文件
- `FileToolsAgent.java` — `FileTools.readWrite(sandboxDir)` + `Tool.fromInstance(...)`
- `FileToolsController.java` — `GET /files/ask`

## 要点

- **务必限定沙箱目录**：文件工具会真实读写宿主机文件。生产上优先 `readOnly`，
  需要写入时也要用独立的临时/工作目录，并注意 `delete` 等破坏性操作。
- 工具是"模型自主决定是否调用"的：可以观察日志里的 tool loop 来确认调用过程。
- 读取内容会进入上下文，大文件应配合分块/摘要策略。
