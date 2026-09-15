package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

/** 链式步骤 1 的产出：提纲。 */
public record Outline(
        @JsonPropertyDescription("文章标题") String title,
        @JsonPropertyDescription("小节标题") List<String> sections) {
}
