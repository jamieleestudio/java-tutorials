package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.common.ai.model.EmbeddingService;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 嵌入与语义检索示例（RAG 的"检索"环节）。
 *
 * <p>{@link Ai#withDefaultEmbeddingService()} 拿到默认嵌入服务（由
 * {@code embabel.models.default-embedding-model} 指定，本示例是 Ollama 的 nomic-embed-text），
 * 用它把文本转成向量，再用余弦相似度做检索——只把最相关的片段放进提示词，
 * 从而在大知识库场景下控制 token 成本。
 *
 * <p>注意：这里每次请求都会重新计算语料的向量（示例共 10 条，够用）。
 * 生产环境应在启动时预计算并缓存，或使用向量数据库。
 */
@Agent(description = "嵌入与语义检索示例：向量检索知识库后据片段回答")
public class EmbeddingsAgent {

    private static final int TOP_K = 3;

    @Action(description = "语义检索并回答")
    @AchievesGoal(description = "产出带依据的回答")
    public SearchResult search(UserInput userInput, Ai ai) {
        EmbeddingService embeddings = ai.withDefaultEmbeddingService();
        float[] queryVector = embeddings.embed(userInput.getContent());

        List<SearchResult.Match> matches = Corpus.DOCUMENTS.stream()
                .map(document -> new SearchResult.Match(
                        cosine(queryVector, embeddings.embed(document)),
                        document))
                .sorted(Comparator.comparingDouble(SearchResult.Match::score).reversed())
                .limit(TOP_K)
                .toList();

        String context = matches.stream()
                .map(match -> "- %s".formatted(match.text()))
                .collect(Collectors.joining("\n"));

        String answer = ai.withDefaultLlm()
                .withId("embeddings-answer")
                .generateText("""
                        请只依据下面的知识片段回答问题；如果片段中没有答案，请明确说明"知识库中未提及"。

                        知识片段：
                        %s

                        问题：%s
                        """.formatted(context, userInput.getContent()));

        return new SearchResult(userInput.getContent(), matches, answer);
    }

    private double cosine(float[] a, float[] b) {
        double dot = 0;
        double normA = 0;
        double normB = 0;
        int length = Math.min(a.length, b.length);
        for (int i = 0; i < length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return (normA == 0 || normB == 0) ? 0.0 : dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
