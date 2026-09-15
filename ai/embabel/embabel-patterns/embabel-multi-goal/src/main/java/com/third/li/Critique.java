package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/** 目标之一：评审意见。 */
public record Critique(
        @JsonPropertyDescription("评审意见") String content) {
}
