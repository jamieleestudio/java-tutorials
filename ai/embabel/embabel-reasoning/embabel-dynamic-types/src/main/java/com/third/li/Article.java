package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/** 一个 JVM 类型（用于和动态类型对照）。 */
public record Article(
        @JsonPropertyDescription("标题") String title,
        @JsonPropertyDescription("标签") String tags,
        @JsonPropertyDescription("评分") double score) {
}
