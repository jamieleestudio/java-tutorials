package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/** 没有 StuckHandler 兜底时的目标类型（用于对照：进程会停在 STUCK）。 */
public record UnhandledReport(
        @JsonPropertyDescription("报告内容") String content) {
}
