package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/** 租户请求：租户 ID + 等级（等级决定用哪个模型）。 */
public record TenantRequest(
        @JsonPropertyDescription("租户 ID") String tenantId,
        @JsonPropertyDescription("等级：basic 或 pro") String tier) {
}
