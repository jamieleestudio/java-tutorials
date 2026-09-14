package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 迭代后的最终产物。
 */
public record RefinedText(
        @JsonPropertyDescription("最终文本") String text,
        @JsonPropertyDescription("最终评分") double score,
        @JsonPropertyDescription("实际迭代次数") int attempts,
        @JsonPropertyDescription("最后一轮的改进建议") String lastSuggestion) {
}
