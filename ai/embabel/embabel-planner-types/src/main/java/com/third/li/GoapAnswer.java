package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * GOAP 规划器的产出。
 */
public record GoapAnswer(
        @JsonPropertyDescription("回答正文") String content,
        @JsonPropertyDescription("使用的规划器") String planner) {
}
