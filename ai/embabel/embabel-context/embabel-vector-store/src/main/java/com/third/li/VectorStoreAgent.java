package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;

import java.util.List;
import java.util.stream.Collectors;

/**
 * RAG 的"检索 + 生成"环节，检索走 **pgvector 持久化向量库**。
 *
 * <p>与 {@code embabel-embeddings}（内存余弦相似度）的区别：
 * 向量已经预先写入 Postgres，这里只做"把问题向量化 -> 近邻查询 -> 拼提示词"，
 * 不需要在每次请求时重算整份语料。
 *
 * <p>另外这里加了**相似度阈值**：低于阈值的片段直接丢弃。
 * 如果全部被丢弃，就明确回答"知识库中未提及"——避免用不相关片段硬编答案。
 */
@Agent(description = "pgvector 向量检索 + 生成（含相似度阈值）")
public class VectorStoreAgent {

    private static final int TOP_K = 3;
    private static final double MIN_SCORE = 0.35;

    private final PgVectorStore store;

    public VectorStoreAgent(PgVectorStore store) {
        this.store = store;
    }

    @Action(description = "检索 pgvector 后回答")
    @AchievesGoal(description = "产出带出处的回答")
    public VectorAnswer ask(UserInput userInput, Ai ai) {
        float[] queryVector = ai.withDefaultEmbeddingService().embed(userInput.getContent());

        List<VectorMatch> hits = store.search(queryVector, TOP_K, null);
        List<VectorMatch> kept = hits.stream()
                .filter(match -> match.score() >= MIN_SCORE)
                .toList();

        String answer;
        if (kept.isEmpty()) {
            answer = "知识库中未提及该问题（最高相似度 %.3f 低于阈值 %.2f）。"
                    .formatted(hits.isEmpty() ? 0.0 : hits.get(0).score(), MIN_SCORE);
        } else {
            String context = kept.stream()
                    .map(match -> "- [%s/%s] %s".formatted(match.source(), match.id(), match.content()))
                    .collect(Collectors.joining("\n"));
            answer = ai.withDefaultLlm()
                    .withId("vector-answer")
                    .generateText("""
                            请只依据下面的知识片段回答问题，并在回答中标注用到的片段来源（如 [internal-wiki/d7]）。
                            如果片段不足以回答，请明确说明。

                            知识片段：
                            %s

                            问题：%s
                            """.formatted(context, userInput.getContent()));
        }

        return new VectorAnswer(userInput.getContent(), kept, answer);
    }
}
