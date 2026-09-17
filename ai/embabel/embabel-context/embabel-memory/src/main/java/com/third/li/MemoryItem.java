package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/** 一条长期记忆。 */
public record MemoryItem(
        @JsonPropertyDescription("记忆 ID") String id,
        @JsonPropertyDescription("类型：preference（偏好）/ fact（事实）") String kind,
        @JsonPropertyDescription("记忆内容") String text,
        @JsonPropertyDescription("召回时的相似度（写入时为 0）") double score) {
}
