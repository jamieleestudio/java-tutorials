package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 本地 Agent 委派远端后的产出（与 {@link ChatReply} 区分开，避免目标类型冲突）。
 */
public record DelegatedReply(
        @JsonPropertyDescription("本地 Agent 的最终回复") String content) {
}
