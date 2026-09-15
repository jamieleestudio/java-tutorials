package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/** 单条用例的评估结果。 */
public record CaseResult(
        @JsonPropertyDescription("用例 ID") String id,
        @JsonPropertyDescription("被测系统的回答") String answer,
        @JsonPropertyDescription("LLM 评审给出的分数 0~1") double score,
        @JsonPropertyDescription("是否通过") boolean passed,
        @JsonPropertyDescription("评审理由") String reason) {
}
