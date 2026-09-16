package com.third.li;

import com.embabel.agent.api.common.AiBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * **两阶段检索的第二阶段：重排（rerank）**。
 *
 * <p>为什么需要：向量相似 ≠ 任务相关。只用余弦距离召回时，top-1 常常不是最相关的
 * （实测：问"只用向量召回有什么问题"，正确片段 `d7 向量检索的召回与重排` 在余弦排序里是**最后一名**）。
 *
 * <p>做法：
 * <ol>
 *   <li><b>粗召回</b>：向量检索取回较多候选（例如 8 条）——快、覆盖广</li>
 *   <li><b>精排</b>：让 LLM 对**每个**候选按"与问题的相关性"打 0~1 分，再排序——准、慢</li>
 *   <li>取前 k 条作为最终上下文</li>
 * </ol>
 *
 * <h3>为什么不用框架自带的 {@code Ranker}（LlmRanker）</h3>
 * 实测读了它的实现后确认：**框架的 `LlmRanker` 是"选择器"而不是"重排器"**——
 * 它的提示词是 <i>"choose the name that best reflects the user's intent"</i>（选**一个**），
 * 所以返回的往往只是最相关的那条，而不是全部候选的新顺序。
 * 它还要求模型返回的名字必须严格存在于候选里，否则直接抛
 * {@code IllegalStateException}（模型写错一个 id 就整次失败）。
 *
 * <p>那个语义适合"从若干目标/候选中挑一个"（见 {@code embabel-multi-goal}），
 * 但**不适合**"把 8 条候选重新排序"。所以本模块自己实现重排：
 * 一次调用给所有候选打分，缺 id 的按 0 分兜底（不会因为模型漏一条就整次失败）。
 *
 * <p>生产上更常见的做法是用 **cross-encoder** 模型（精度更高、无需 LLM 提示词），
 * 代价是要额外部署一个模型服务。
 */
@Component
public class Reranker {

    private static final Logger log = LoggerFactory.getLogger(Reranker.class);

    private final AiBuilder aiBuilder;

    public Reranker(AiBuilder aiBuilder) {
        this.aiBuilder = aiBuilder;
    }

    /**
     * 对候选做完整重排（返回全部候选，按 LLM 相关性降序，score 替换为 LLM 评分）。
     *
     * @param goal       用户的问题（作为相关性依据）
     * @param candidates 粗召回得到的候选
     */
    public List<VectorMatch> rerank(String goal, List<VectorMatch> candidates) {
        if (candidates.size() <= 1) {
            return candidates;
        }

        String listing = candidates.stream()
                .map(match -> "- id=%s | %s".formatted(match.id(), match.content()))
                .collect(Collectors.joining("\n"));

        RerankScores scores = aiBuilder.ai().withDefaultLlm()
                .withId("vector-rerank")
                .creating(RerankScores.class)
                .fromPrompt("""
                        请对下面每个知识片段与「用户问题」的相关性打分（0~1，1 表示直接回答该问题）。
                        必须为每个片段返回一条评分，id 原样照抄。

                        用户问题：%s

                        候选片段：
                        %s
                        """.formatted(goal, listing));

        Map<String, Double> byId = scores.scores().stream()
                .collect(Collectors.toMap(
                        RerankScores.Score::id,
                        RerankScores.Score::score,
                        (a, b) -> a));

        List<VectorMatch> ordered = candidates.stream()
                .map(match -> match.withScore(byId.getOrDefault(match.id(), 0.0)))
                .sorted(Comparator.comparingDouble(VectorMatch::score).reversed())
                .toList();

        log.info("重排完成：{} 条候选 -> 新顺序 {}", candidates.size(),
                ordered.stream().map(VectorMatch::id).toList());
        return ordered;
    }

    /** 只取重排后的前 k 条。 */
    public List<VectorMatch> rerankAndTake(String goal, List<VectorMatch> candidates, int k) {
        return rerank(goal, candidates).stream().limit(k).toList();
    }

    /** 便于单测：把候选转成 id->score 的函数。 */
    static Function<VectorMatch, Double> scoreOf(Map<String, Double> byId) {
        return match -> byId.getOrDefault(match.id(), 0.0);
    }
}
