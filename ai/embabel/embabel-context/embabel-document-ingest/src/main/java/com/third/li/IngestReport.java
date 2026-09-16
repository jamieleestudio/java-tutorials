package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

/** 一次摄入的结果报告。 */
public record IngestReport(
        @JsonPropertyDescription("扫描到的文件数") int scanned,
        @JsonPropertyDescription("重新摄入的文件数（内容有变化）") int ingested,
        @JsonPropertyDescription("跳过的文件数（内容未变）") int skipped,
        @JsonPropertyDescription("当前向量库里的块总数") int totalChunks,
        @JsonPropertyDescription("分块参数") String chunking,
        @JsonPropertyDescription("每个文件的处理情况") List<DocDetail> details,
        @JsonPropertyDescription("耗时（毫秒）") long elapsedMillis) {

    /** 单个文件的处理情况。 */
    public record DocDetail(
            @JsonPropertyDescription("文档 ID") String docId,
            @JsonPropertyDescription("来源路径") String source,
            @JsonPropertyDescription("块数") int chunks,
            @JsonPropertyDescription("状态：INGESTED / SKIPPED / FAILED") String status) {
    }
}
