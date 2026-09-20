package com.third.li;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 向量嵌入（EmbeddingModel）。
 *
 * <p>{@link EmbeddingModel} 把文本映射成向量：语义相近的文本向量距离更近，
 * 这是 RAG 检索的地基。本模块用 OpenAI 兼容端点调用嵌入模型
 * （默认 DashScope 兼容模式 + text-embedding-v3，可用 EMBEDDING_BASE_URL /
 * DASHSCOPE_API_KEY / EMBEDDING_MODEL 环境变量换成任意 OpenAI 兼容嵌入服务）。
 *
 * <p>模型无关是 Spring AI 的核心设计：换成 SAA 的 DashScope 原生 starter
 * 只需换依赖与配置，业务代码里的 EmbeddingModel API 不变。
 */
@RestController
public class EmbeddingController {

    private final EmbeddingModel embeddingModel;

    public EmbeddingController(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    /** 单文本嵌入。 */
    @GetMapping("/ai/embedding")
    public float[] embed(
            @RequestParam(value = "text", defaultValue = "Spring AI Alibaba 是 Spring AI 的超集") String text) {
        return embeddingModel.embed(text);
    }

    /** Document 嵌入。 */
    @GetMapping("/ai/embedding/document")
    public float[] embedDocument(
            @RequestParam(value = "text", defaultValue = "Graph 编排把工作流变成图") String text) {
        return embeddingModel.embed(new Document(text));
    }

    /** 批量嵌入 + 维度信息。 */
    @GetMapping("/ai/embedding/response")
    public EmbeddingResponse embedBatch(
            @RequestParam(value = "text", defaultValue = "第一句话") String text) {
        return embeddingModel.embedForResponse(java.util.List.of(text, "第二句话"));
    }
}
