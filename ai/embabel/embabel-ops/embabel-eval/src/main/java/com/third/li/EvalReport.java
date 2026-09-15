package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

/** 评估报告。 */
public record EvalReport(
        @JsonPropertyDescription("用例总数") int total,
        @JsonPropertyDescription("通过数（score >= 0.7）") int passed,
        @JsonPropertyDescription("平均分") double averageScore,
        @JsonPropertyDescription("逐用例结果") List<CaseResult> results) {
}
