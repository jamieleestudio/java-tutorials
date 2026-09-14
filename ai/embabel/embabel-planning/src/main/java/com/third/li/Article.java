package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 最终产物：规划流水线的目标类型。
 */
public record Article(
        @JsonPropertyDescription("文章标题") String title,
        @JsonPropertyDescription("文章正文") String content) {
}
