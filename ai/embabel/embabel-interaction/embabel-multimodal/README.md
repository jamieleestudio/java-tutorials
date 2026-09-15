# embabel-multimodal — 多模态（图像理解）

## 演示内容

把图片和提示词一起发给**视觉模型**，让模型"看图说话"。
示例会在启动时用 Java2D 生成一张测试图（蓝色矩形 + 红色圆形 + 文字 `EMBABEL`），无需外部图片资源。

## 关键 API

| API | 作用 |
|---|---|
| `AgentImage.fromFile(File)` / `fromPath(Path)` / `fromBytes(mime, bytes)` | 构造图片输入 |
| `MultimodalContent.withImage(text, image)` | 文本 + 图片组成一次多模态输入 |
| `promptRunner.generateText(MultimodalContent)` | 发送多模态内容 |
| `ai.withLlm("qwen2.5vl:3b")` | 指定支持视觉的模型 |
| `AgentDocument` + `MultimodalContent.withDocument(...)` | 文档（PDF 等）同理 |

## 前置条件（Docker）

DeepSeek 无视觉能力，需要本地视觉模型：

```bash
cd ai/embabel/docker
cp .env.example .env
docker compose up -d litellm ollama
docker compose exec ollama ollama pull qwen2.5vl:3b
```

运行本模块时把环境变量指向 LiteLLM：

```powershell
$env:OPENAI_BASE_URL="http://localhost:4000"; $env:OPENAI_API_KEY="sk-1234"
```

## 接口

```bash
# 使用启动时生成的示例图片
curl http://localhost:8908/multimodal/describe

# 或指定本机图片
curl "http://localhost:8908/multimodal/describe?imagePath=D:/pics/demo.png"
```

实测返回：

```json
{"imagePath":"...sample.png","description":"图片中有一个蓝色的矩形和一个红色的圆形。图片下方有黑色的文字：EMBABEL。"}
```

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-multimodal spring-boot:run
```

## 代码结构

- `SampleImageGenerator.java` — 启动时生成测试图片（Java2D，headless 可用）
- `MultimodalAgent.java` — `AgentImage.fromFile(...)` + `MultimodalContent.withImage(...)`
- `MultimodalController.java` — `GET /multimodal/describe`

## 要点

- 图片会随请求发送，注意**体积与 token 成本**；大图建议先压缩。
- 视觉能力取决于模型，不是所有 OpenAI 兼容模型都支持；`withLlm(...)` 显式指定最稳妥。
- 首次调用较慢（Ollama 需要把模型载入内存，通常 30–60s）。
