package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/** Sectioning 的汇总产出（目标类型）。 */
public record Report(
        @JsonPropertyDescription("汇总报告") String content,
        @JsonPropertyDescription("参与评审的维度数") int aspects) {
}
