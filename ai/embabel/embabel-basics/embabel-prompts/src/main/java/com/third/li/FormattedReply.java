package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/** 带格式说明的回答（@Provided 演示用）。 */
public record FormattedReply(
        @JsonPropertyDescription("回答正文") String content,
        @JsonPropertyDescription("使用的格式") String format) {
}
