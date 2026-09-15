package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 模型回退示例的回答。
 */
public record FallbackAnswer(
        @JsonPropertyDescription("回答正文") String content,
        @JsonPropertyDescription("实际尝试的模型顺序") String routing) {
}
