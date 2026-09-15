package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

/**
 * 一次运行的观测报告。
 */
public record RunReport(
        @JsonPropertyDescription("进程终态") String status,
        @JsonPropertyDescription("动作步骤及耗时") List<String> steps,
        @JsonPropertyDescription("本次运行累计成本（美元）") double cost,
        @JsonPropertyDescription("本次运行累计 token 数") Integer totalTokens,
        @JsonPropertyDescription("使用到的模型") List<String> models,
        @JsonPropertyDescription("监听器捕获的事件") List<String> events,
        @JsonPropertyDescription("监听器累计的 LLM 调用次数") int llmCalls,
        @JsonPropertyDescription("监听器累计的动作执行次数") int actionRuns) {
}
