package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/** LLM 评审结果（结构化输出）。 */
public record Judge(
        @JsonPropertyDescription("分数 0.0~1.0") double score,
        @JsonPropertyDescription("评审理由") String reason) {
}
