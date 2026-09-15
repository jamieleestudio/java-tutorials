package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

/** Voting 的产出（目标类型）。 */
public record Verdict(
        @JsonPropertyDescription("最终判定") String decision,
        @JsonPropertyDescription("各次独立判定的结果") List<String> votes,
        @JsonPropertyDescription("说明") String rationale) {
}
