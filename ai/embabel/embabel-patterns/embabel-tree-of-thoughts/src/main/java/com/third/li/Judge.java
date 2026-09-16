package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/** 评分器输出（结构化）。 */
public record Judge(
        @JsonPropertyDescription("可行性评分 0~1") double score,
        @JsonPropertyDescription("评分理由") String reason) {
}
