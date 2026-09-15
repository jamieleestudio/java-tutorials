# embabel-prompts — 提示词工程（模板 / 人格 / 公共提示 / @Provided）

## 演示内容

把提示词当**工程资产**来管理，而不是散落在字符串里：

1. **Jinja 模板**：提示词放 `src/main/resources/prompts/<name>.jinja`，用 `rendering("<name>")` 渲染
2. **人格（PersonaSpec）**：`PersonaSpec.create(name, persona, voice, objective)` 结构化地描述"你是谁"
3. **公共提示（PromptContributor）**：团队统一规范（如"必须给来源"）一次定义、处处生效
4. **`@Provided`**：把 Spring 组件注入动作方法参数（在 `@State` 类里尤其有用）

## 关键 API

| API | 作用 |
|---|---|
| `promptRunner.rendering("answer")` | 渲染 `classpath:/prompts/answer.jinja`，变量用 Map 传入 |
| `Rendering.generateText(model)` / `.createObject(T.class, model)` | 渲染模板并调用 LLM |
| `withPromptElements(personaSpec)` | 注入人格（`PromptElement`） |
| `withPromptContributor(PromptContributor.fixed(...))` | 注入固定/动态提示片段 |
| `@Provided`（参数注解） | 参数由平台/Spring 提供，而非从黑板解析 |

## 接口

```bash
# 模板 + 人格 + 公共提示
curl -G --data-urlencode "message=单体应用什么时候该拆成微服务？" http://localhost:8921/prompts/ask

# @Provided 注入 Spring 组件
curl -G --data-urlencode "message=什么是提示词模板？" http://localhost:8921/prompts/provided
```

实测：`/prompts/provided` 返回 `{"content":"【格式：Markdown】\n...","format":"Markdown"}`——
说明注入的 `ReplyFormatter` 生效了。

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-prompts spring-boot:run
```

## 代码结构

- `PromptAgent.java` — PersonaSpec + PromptContributor + `rendering("answer")`
- `ProvidedDemoAgent.java` — `@Provided ReplyFormatter` 注入
- `src/main/resources/prompts/answer.jinja` — 提示词模板
- `PromptsController.java` — 两个端点

## 要点

- **模板位置固定**：`JinjaTemplateRenderer` 从 `classpath:/prompts/` 读取、后缀 `.jinja`。
- 模板适合"可评审、可版本化"的提示词；人格适合"角色/语气/目标"这类结构化设定；
  公共提示适合跨模块的统一约束——三者可叠加。
- `@Provided` 解决的是"动作方法拿不到外层依赖"的问题（`@State` 类最常见）。
- 提示词改动要配 **eval 回归**（见 `embabel-eval`），否则容易改坏而不自知。
