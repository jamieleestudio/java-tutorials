package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.Map;

/** 状态机处理结果（目标类型）。 */
public record OrderResult(
        @JsonPropertyDescription("原始订单请求") String request,
        @JsonPropertyDescription("状态机执行后的总结") String output,
        @JsonPropertyDescription("各状态下可用的工具数量（体现按状态收敛工具集）") Map<String, Integer> toolsPerState) {
}
