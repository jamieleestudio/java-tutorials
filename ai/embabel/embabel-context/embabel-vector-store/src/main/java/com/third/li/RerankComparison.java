package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

/** 两阶段检索的对照结果：同一批候选的"纯召回顺序" vs "重排后顺序"。 */
public record RerankComparison(
        @JsonPropertyDescription("问题") String question,
        @JsonPropertyDescription("第一阶段：向量粗召回（按余弦相似度，score 是相似度）") List<VectorMatch> recalled,
        @JsonPropertyDescription("第二阶段：LLM 精排（score 是 LLM 给出的相关性评分）") List<VectorMatch> reranked,
        @JsonPropertyDescription("说明") String note) {
}
