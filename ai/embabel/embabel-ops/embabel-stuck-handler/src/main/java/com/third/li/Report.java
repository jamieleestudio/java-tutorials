package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/** 有 StuckHandler 兜底时的目标类型。 */
public record Report(
        @JsonPropertyDescription("报告内容") String content) {
}
