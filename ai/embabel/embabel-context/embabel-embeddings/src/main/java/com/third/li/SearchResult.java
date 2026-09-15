package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

/**
 * 语义检索 + 生成的结果。
 */
public record SearchResult(
        @JsonPropertyDescription("用户问题") String query,
        @JsonPropertyDescription("命中的知识片段及相似度") List<Match> matches,
        @JsonPropertyDescription("基于命中片段的回答") String answer) {

    /** 单条命中。 */
    public record Match(
            @JsonPropertyDescription("相似度，0~1，越大越相似") double score,
            @JsonPropertyDescription("知识片段内容") String text) {
    }
}
