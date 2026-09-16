package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 自省后的报告（目标类型）。
 *
 * <p>注意：本模块里有两个 Agent——{@link ToolsAdvancedAgent} 的目标类型是 {@link Reply}，
 * {@link IntrospectiveAgent} 的目标类型是这个 {@code Introspection}。
 * 目标类型是 `AgentInvocation.create(platform, X.class)` **选择 Agent 的唯一依据**
 * （框架按 `findAgentByResultType` 匹配），所以**两个 Agent 不能共用同一个目标类型**，
 * 否则会命中歧义。这里刻意用不同类型来演示这一点。
 */
public record Introspection(
        @JsonPropertyDescription("自省后给出的回答正文") String content) {
}
