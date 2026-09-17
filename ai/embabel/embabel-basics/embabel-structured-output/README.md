# embabel-structured-output — 结构化输出

## 演示内容

把自由文本转成**强类型 Java 对象**：`PromptRunner.creating(T.class)` 让 LLM 按目标类型返回内容，
Embabel 负责解析、映射与校验。

## 关键 API

| API | 作用 |
|---|---|
| `creating(Profile.class)` | 声明期望的输出类型 |
| `.withExample(desc, value)` | 提供示例，提升输出稳定性 |
| `.withValidation(true)` | 开启结果校验 |
| `.fromPrompt(prompt)` | 执行并直接拿到强类型对象 |
| `@JsonPropertyDescription` | 字段说明会写进提示词，帮助模型理解每个字段 |

## 接口

```bash
curl -G --data-urlencode "text=你好，我是李四，今年 28 岁，平时主要用 Java 和 Kubernetes" http://localhost:8892/extract
```

返回 `Profile` 的 JSON：

```json
{"name":"李四","age":28,"skills":["Java","Kubernetes"],"summary":"..."}
```

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-structured-output spring-boot:run
```

## 代码结构

- `Profile.java` — 输出模型（record + `@JsonPropertyDescription`）
- `ProfileAgent.java` — `@Action` 中 `creating(Profile.class).withExample(...).withValidation(true)`
- `ExtractController.java` — `GET /extract`，直接返回强类型对象

## 要点

- **DeepSeek 注意**：DeepSeek 只支持 `response_format={"type":"json_object"}`，不支持 `json_schema`。
  本模块的 `models/openai-models.yml` 关闭了原生结构化输出（`supported: false`），
  Embabel 自动回退为"提示词 + 解析"的方案，用法不变。
- `withValidation` 与 `withProperties/withoutProperties` 可精细控制生成字段。


## 附：字段级校验（未单独建模块）

本模块讲"把模型输出绑定到强类型"。绑定的对象还可以带**字段级校验规则**：

| API | 作用 |
|---|---|
| `PropertyValidationRule` | `isValid(String)` + `failureReason(String)`，逐字段校验 |
| `ValidatedPropertyDefinition` | 带校验规则的字段定义（`ValuePropertyDefinition` 的扩展） |
| `PropertyDefinition.getMetadata()` | 语义元数据（`@Semantics`/`@With` 填充） |

用法：把校验规则放进 `ValidatedPropertyDefinition`，绑定失败时能给出**字段级**错误
（而不是笼统的"解析失败"），UX 层就能精确地高亮出错字段。
"校验失败 → 让模型重试"这条链路在 `embabel-hitl-advanced` 的 `ValidationError` 里能看到同样的思路。
