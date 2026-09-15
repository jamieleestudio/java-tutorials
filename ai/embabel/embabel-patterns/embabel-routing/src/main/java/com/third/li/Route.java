package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 分类结果。
 *
 * <p>{@code kind} 用字符串而不是枚举：提示词方案下模型可能返回中文，
 * 条件里同时接受英文与中文（见 {@link RoutingAgent}）。
 */
public record Route(
        @JsonPropertyDescription("类别，必须是 REFUND / TECH / GENERAL 之一") String kind,
        @JsonPropertyDescription("分类理由") String reason) {
}
