package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

/** 上线手册执行结果（目标类型）。 */
public record PlaybookOutcome(
        @JsonPropertyDescription("模型总结") String summary,
        @JsonPropertyDescription("实际调用过的工具（按顺序）") List<String> toolCalls,
        @JsonPropertyDescription("初始解锁的工具数") int unlockedTools,
        @JsonPropertyDescription("初始锁定的工具数") int lockedTools,
        @JsonPropertyDescription("说明") String note) {
}
