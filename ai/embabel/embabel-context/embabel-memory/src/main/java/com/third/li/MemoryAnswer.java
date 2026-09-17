package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

/** 带记忆的回答（目标类型）。 */
public record MemoryAnswer(
        @JsonPropertyDescription("用户 ID") String userId,
        @JsonPropertyDescription("问题") String question,
        @JsonPropertyDescription("本次召回到的记忆") List<MemoryItem> recalled,
        @JsonPropertyDescription("回答") String answer) {
}
