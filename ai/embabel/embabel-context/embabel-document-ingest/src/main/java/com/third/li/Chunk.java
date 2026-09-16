package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 一个**文本块**（入库与检索的最小单位）。
 *
 * <p>{@code chunkId} = {@code docId + "#" + seq}，所以重新分块时能精确覆盖旧块。
 * 保留 {@code docId}/{@code source}/{@code seq} 是为了**回溯原文**
 * （回答时能说"来自某文档的第 3 段"）。
 */
public record Chunk(
        @JsonPropertyDescription("块 ID") String chunkId,
        @JsonPropertyDescription("所属文档 ID") String docId,
        @JsonPropertyDescription("来源路径") String source,
        @JsonPropertyDescription("标题") String title,
        @JsonPropertyDescription("块序号（从 0 开始）") int seq,
        @JsonPropertyDescription("块文本") String text) {

    public static String idOf(String docId, int seq) {
        return docId + "#" + seq;
    }
}
