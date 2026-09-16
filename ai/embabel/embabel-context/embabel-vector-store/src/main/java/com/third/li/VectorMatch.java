package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/** 一次向量检索的命中结果。 */
public record VectorMatch(
        @JsonPropertyDescription("文档 ID") String id,
        @JsonPropertyDescription("标题") String title,
        @JsonPropertyDescription("内容") String content,
        @JsonPropertyDescription("来源（元数据）") String source,
        @JsonPropertyDescription("余弦相似度（1 - 余弦距离），越大越相关") double score) {
}
