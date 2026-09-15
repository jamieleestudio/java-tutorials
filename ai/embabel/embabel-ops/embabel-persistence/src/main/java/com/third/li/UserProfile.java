package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 需要跨会话/跨重启保留的用户画像（会被存进 Context）。
 */
public record UserProfile(
        @JsonPropertyDescription("用户 ID") String userId,
        @JsonPropertyDescription("称呼") String name,
        @JsonPropertyDescription("订阅计划") String plan) {
}
