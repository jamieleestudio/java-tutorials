package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

/** 重排评分结果（结构化输出）。 */
public record RerankScores(
        @JsonPropertyDescription("每个候选片段的评分") List<Score> scores) {

    public record Score(
            @JsonPropertyDescription("片段 id，必须原样返回候选里给的 id") String id,
            @JsonPropertyDescription("与用户问题的相关性，0~1") double score) {
    }
}
