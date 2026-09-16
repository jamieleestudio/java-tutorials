package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/** 搜索树上的一个候选思路（含评分）。 */
public record Candidate(
        @JsonPropertyDescription("所在层级，例如 第1层") String level,
        @JsonPropertyDescription("思路内容") String approach,
        @JsonPropertyDescription("可行性评分 0~1") double score,
        @JsonPropertyDescription("评分理由") String reason) {
}
