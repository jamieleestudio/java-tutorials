package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * UTILITY 规划器的产出。
 */
public record UtilityAnswer(
        @JsonPropertyDescription("回答正文") String content,
        @JsonPropertyDescription("使用的规划器") String planner) {
}
