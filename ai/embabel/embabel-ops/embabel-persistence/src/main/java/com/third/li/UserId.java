package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 领域对象：用户标识（作为 Agent 的输入之一）。
 */
public record UserId(
        @JsonPropertyDescription("用户 ID") String value) {
}
