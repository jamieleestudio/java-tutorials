package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 子 Agent 的输出类型。
 */
public record Translation(
        @JsonPropertyDescription("译文") String translatedText,
        @JsonPropertyDescription("目标语言") String targetLanguage) {
}
