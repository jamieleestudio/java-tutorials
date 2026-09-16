package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/** 第 1 步产物：需求分析要点。 */
public record Analysis(
        @JsonPropertyDescription("分析要点") String points) {
}
