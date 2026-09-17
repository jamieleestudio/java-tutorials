package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/** 结构化输出的目标类型：一份产品分析报告。 */
public record ProductAnalysis(
        @JsonPropertyDescription("产品名称") String productName,
        @JsonPropertyDescription("优势列表") java.util.List<String> strengths,
        @JsonPropertyDescription("劣势列表") java.util.List<String> weaknesses,
        @JsonPropertyDescription("综合评分，0~10") double rating,
        @JsonPropertyDescription("一句话结论") String conclusion) {
}