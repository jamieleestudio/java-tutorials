package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/** 处理结果（目标类型）。 */
public record Reply(
        @JsonPropertyDescription("实际命中的处理通道") String channel,
        @JsonPropertyDescription("回复正文") String content) {
}
