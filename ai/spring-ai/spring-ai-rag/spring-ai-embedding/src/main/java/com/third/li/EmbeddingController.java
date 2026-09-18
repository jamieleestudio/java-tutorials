package com.third.li;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 向量嵌入（EmbeddingModel + Document）。
 *
 * <p>{@link EmbeddingModel} 把文本转成向量：
 * <ul>
 *   <li>{@code embed(String)} — 直接返回 float[] 向量</li>
 *   <li>{@code call(EmbeddingRequest)} — 返回完整 {@link EmbeddingResponse}</li>
 *   <li>{@code embed(Document)} — 处理 {@link Document}（带元数据）</li>
 * </ul>
 *
 * <p>嵌入是 RAG 的基础：文本 → 向量 → 相似度检索。
 * 配置：<code>spring.ai.openai.embedding.options.model=text-embedding-3-small</code>
 * （DeepSeek 也提供 embedding 兼容接口）。
 */
@RestController
public class EmbeddingController {

    private final EmbeddingModel embeddingModel;

    public EmbeddingController(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    /** 嵌入文本，返回向量维度 + 前几个值。 */
    @GetMapping("/ai/embedding")
    public String embed(
            @RequestParam(value = "text", defaultValue = "Spring AI 是 Java 的 AI 框架") String text) {
        float[] vector = embeddingModel.embed(text);
        return "向量维度：" + vector.length + "\n前 5 个值：" + java.util.Arrays.toString(
                java.util.Arrays.copyOfRange(vector, 0, Math.min(5, vector.length)));
    }

    /** 嵌入 Document（带元数据）。 */
    @GetMapping("/ai/embedding/document")
    public String embedDocument(
            @RequestParam(value = "text", defaultValue = "向量数据库用于相似度检索") String text) {
        Document doc = new Document(text, java.util.Map.of("source", "tutorial"));
        float[] vector = embeddingModel.embed(doc);
        return "Document 向量维度：" + vector.length + "，元数据：" + doc.getMetadata();
    }

    /** 返回完整 EmbeddingResponse。 */
    @GetMapping("/ai/embedding/response")
    public String embedResponse(
            @RequestParam(value = "text", defaultValue = "嵌入模型把文本编码为向量") String text) {
        EmbeddingResponse response = embeddingModel.embedForResponse(java.util.List.of(text));
        return "结果数：" + response.getResults().size()
                + "，维度：" + response.getResult().getOutput().length
                + "，索引：" + response.getResult().getIndex();
    }
}
