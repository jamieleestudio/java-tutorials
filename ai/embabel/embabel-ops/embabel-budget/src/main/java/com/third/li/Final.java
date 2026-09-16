package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/** 第 3 步产物：定稿（目标类型）。 */
public record Final(
        @JsonPropertyDescription("最终方案") String content) {
}
