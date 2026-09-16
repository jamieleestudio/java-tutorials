package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/** 回答（目标类型）。 */
public record TenantAnswer(
        @JsonPropertyDescription("租户 ID") String tenantId,
        @JsonPropertyDescription("本次使用的模型等级") String tier,
        @JsonPropertyDescription("回答正文") String content) {
}
