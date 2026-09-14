package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 子 Agent 的输入类型。
 */
public record TranslationRequest(
        @JsonPropertyDescription("待翻译的文本") String text,
        @JsonPropertyDescription("目标语言，例如：英文/日语") String targetLanguage) {
}
