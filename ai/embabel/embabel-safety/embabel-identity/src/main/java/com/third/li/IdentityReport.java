package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.Map;

/** 身份与上下文报告（目标类型）。 */
public record IdentityReport(
        @JsonPropertyDescription("当前用户（forUser）") Map<String, String> forUser,
        @JsonPropertyDescription("以谁的身份执行（runAs，可为空）") Map<String, String> runAs,
        @JsonPropertyDescription("请求级元数据（ToolCallContext）") Map<String, Object> toolCallContext,
        @JsonPropertyDescription("租户隔离工具的返回") String scopedResult,
        @JsonPropertyDescription("不读上下文的工具返回（反面教材）") String leakyResult) {
}
