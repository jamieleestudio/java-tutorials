package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/** 第 2 步产物：草稿。 */
public record Draft(
        @JsonPropertyDescription("草稿内容") String content) {
}
