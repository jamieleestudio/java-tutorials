package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/** 综合后的最终报告（目标类型）。 */
public record FinalReport(
        @JsonPropertyDescription("最终报告") String content,
        @JsonPropertyDescription("实际拆解出的子任务数") int subtaskCount) {
}
