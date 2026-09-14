package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

/**
 * 文章提纲：规划流水线的第二个中间产物。
 */
public record Outline(
        @JsonPropertyDescription("文章标题") String title,
        @JsonPropertyDescription("各章节标题") List<String> sections) {
}
