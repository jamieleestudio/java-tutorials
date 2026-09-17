package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;
import java.util.Map;

/** 一次运行 + 收集到的 span。 */
public record SpanReport(
        @JsonPropertyDescription("Agent 输出") String output,
        @JsonPropertyDescription("收集到的 span 数") int spanCount,
        @JsonPropertyDescription("span 列表（按结束顺序）") List<Span> spans,
        @JsonPropertyDescription("说明") String note) {

    /** 一个被记录下来的 span。 */
    public record Span(
            @JsonPropertyDescription("span 名（来自 Observation.Context）") String name,
            @JsonPropertyDescription("耗时（毫秒）") double durationMillis,
            @JsonPropertyDescription("是否出错") boolean error,
            @JsonPropertyDescription("低基数标签") Map<String, String> tags) {
    }
}
