package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 一份**原始文档**（已抽出纯文本）。
 *
 * @param docId       稳定标识（这里用文件名，保证重复摄入时能对上）
 * @param source      相对路径，便于回溯原文
 * @param title       标题（取自首个 Markdown 标题或文件名）
 * @param text        抽出的纯文本
 * @param contentHash 内容的 SHA-256，**增量摄入的依据**
 */
public record RawDocument(
        @JsonPropertyDescription("文档 ID") String docId,
        @JsonPropertyDescription("来源路径") String source,
        @JsonPropertyDescription("标题") String title,
        @JsonPropertyDescription("纯文本") String text,
        @JsonPropertyDescription("内容哈希") String contentHash) {
}
