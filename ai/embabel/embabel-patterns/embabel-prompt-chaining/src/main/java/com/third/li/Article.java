package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/** 链式步骤 3 的产出：文章（目标类型）。 */
public record Article(
        @JsonPropertyDescription("文章标题") String title,
        @JsonPropertyDescription("文章正文") String content) {
}
