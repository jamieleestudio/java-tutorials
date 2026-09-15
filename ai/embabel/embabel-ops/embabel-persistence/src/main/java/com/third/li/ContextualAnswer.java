package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 结合了持久化上下文的回答。
 */
public record ContextualAnswer(
        @JsonPropertyDescription("回答正文") String content,
        @JsonPropertyDescription("本次从 Context 读到的用户画像") UserProfile profile) {
}
