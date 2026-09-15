# embabel-thinking — 推理过程提取

## 演示内容

把模型的**推理过程**与**最终答案**分开返回。DeepSeek V4.1 Flash 默认开启思考模式，
非常适合演示这一能力。

## 关键 API

| API | 作用 |
|---|---|
| `LlmOptions.Companion.withDefaultLlm().withThinking(Thinking.withExtraction())` | 打开 thinking 提取 |
| `promptRunner.supportsThinking()` | 先探测是否支持 |
| `promptRunner.thinking().generateText(prompt)` | 返回 `ThinkingResponse<String>` |
| `ThinkingResponse.getResult()` / `getThinkingContent()` / `hasThinking()` | 答案 / 推理内容 / 是否有推理 |

## 接口

```bash
curl -G --data-urlencode "message=鸡兔同笼：共 35 只，脚共 94 只，各有多少只？" http://localhost:8896/thinking/ask
```

返回：

```json
{"answer":"...","thinkingExtracted":true,"thinking":"...","note":null}
```

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-thinking spring-boot:run
```

## 代码结构

- `ThinkingAgent.java` — 打开 thinking 提取并对不支持的情况做降级
- `Analysis.java` — 返回结构：答案 + 是否提取到 + 推理内容 + 降级说明
- `ThinkingController.java` — `GET /thinking/ask`

## 要点（务必注意）

- **Embabel 的提取器识别的是内容中的 `<think>...</think>` 标签**，而 DeepSeek 的原生推理在独立的
  `reasoning_content` 字段里。所以提示词里要显式要求模型"把推理写在 `<think></think>` 标签中"，
  否则 `thinkingExtracted` 会是 false。
- 一定先判断 `supportsThinking()` 再调用 `thinking()`，并在不支持时降级。
- `withTokenBudget(...)` 可给思考过程设置 token 预算。
