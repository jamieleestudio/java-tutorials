# embabel-references — 引用加载（轻量 RAG）

## 演示内容

把参考资料作为 `LlmReference` 注入提示词，让模型"依据资料"回答，并标注来源。
适合**小体量**知识（文档片段、术语表、接口说明）。

## 关键 API

| API | 作用 |
|---|---|
| `LlmReference` | 既是提示词贡献者，也可携带工具 |
| `promptRunner.withReferences(...)` / `withReference(...)` | 注入参考资料 |
| `SpringResource` | 读取 classpath/文件资源内容（框架内置） |
| `LiteralText` | 内联一段文本（框架内置） |
| `WebPage` | 引用网页，需要 web 工具组 `CoreToolGroups.WEB`（brave/fetch 等工具） |
| `EagerSearch` | 需要 embedding/向量检索时的接口（大知识库场景） |

## 接口

```bash
curl -G --data-urlencode "message=动作（Action）和黑板（Blackboard）分别是什么？" \
     http://localhost:8901/references/ask
```

返回 `{"answer":"...","references":["embabel-handbook","glossary"]}`。

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-references spring-boot:run
```

## 代码结构

- `DocumentReference.java` — 自定义 `LlmReference`（只输出名称/说明/内容）
- `ReferencesAgent.java` — 注入 classpath 文档 + 内联术语表后回答
- `src/main/resources/docs/embabel-handbook.md` — 模拟内部知识文档
- `ReferencesController.java` — `GET /references/ask`

## 要点（踩坑记录）

- **不要直接给"纯文本"用内置的 `SpringResource`/`LiteralText`**：它们的默认
  `contribution()` 里会带一行 `Tool prefix: xxx`，而它们本身不提供工具。
  DeepSeek 看到这行会"脑补"出 `<tool_calls><invoke name="xxx__read">` 之类的内容，
  但工具列表实际为空（日志里 `starting tool loop []`），调用无法执行，回答直接不可用。
  本模块因此自定义了 `DocumentReference`（见其 Javadoc）。内置实现更适合**确实带工具**的引用。
- 参考内容会**全量**进入提示词：内容越长，token 成本越高。大知识库请用 embedding + 向量库。
- 提示词里明确写"资料已包含在提示词中，无需调用任何工具"可进一步避免误触发。
