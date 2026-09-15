package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 单轮对话结果。
 */
public record ChatResult(
        @JsonPropertyDescription("会话 ID") String sessionId,
        @JsonPropertyDescription("助手回复") String reply,
        @JsonPropertyDescription("当前会话消息总数") int messageCount) {
}
