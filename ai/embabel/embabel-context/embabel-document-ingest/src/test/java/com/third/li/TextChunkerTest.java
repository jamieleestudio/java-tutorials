package com.third.li;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link TextChunker} 的确定性单测——**不需要 API Key、不需要 Docker**，所以能进 CI。
 *
 * <p>分块是 RAG 里最影响效果的一步，而它是个**纯函数**，非常适合单测。
 * 这也是本仓库里"值得写测试"的典型：纯逻辑 + 边界多。
 */
class TextChunkerTest {

    private RawDocument doc(String text) {
        return new RawDocument("d1", "d1.md", "测试文档", text, "hash");
    }

    @Test
    @DisplayName("按空行切段后聚合到目标大小")
    void aggregatesParagraphsUpToTargetSize() {
        TextChunker chunker = new TextChunker(100, 0);
        // 每段约 60 字，两段就该触发一次落块
        String text = "一".repeat(60) + "\n\n" + "二".repeat(60) + "\n\n" + "三".repeat(60);

        List<Chunk> chunks = chunker.chunk(doc(text));

        assertThat(chunks).hasSizeGreaterThanOrEqualTo(2);
        assertThat(chunks.get(0).seq()).isZero();
        assertThat(chunks.get(0).chunkId()).isEqualTo("d1#0");
    }

    @Test
    @DisplayName("相邻块之间保留重叠，且重叠取自上一块尾部")
    void keepsOverlapBetweenChunks() {
        TextChunker chunker = new TextChunker(60, 20);
        String first = "A".repeat(60);
        String second = "B".repeat(60);
        String third = "C".repeat(60);

        List<Chunk> chunks = chunker.chunk(doc(first + "\n\n" + second + "\n\n" + third));

        assertThat(chunks).hasSizeGreaterThanOrEqualTo(2);
        // 第二块的开头应当包含上一块的结尾（A），即发生了重叠
        assertThat(chunks.get(1).text()).startsWith("A");
    }

    @Test
    @DisplayName("单段超长时硬切，避免出现一个巨块")
    void hardSplitsOverlongParagraph() {
        TextChunker chunker = new TextChunker(50, 0);
        String oneHugeParagraph = "X".repeat(500);   // 远超 2×目标大小

        List<Chunk> chunks = chunker.chunk(doc(oneHugeParagraph));

        assertThat(chunks).hasSizeGreaterThan(1);
        assertThat(chunks).allSatisfy(chunk ->
                assertThat(chunk.text().length()).isLessThanOrEqualTo(100));
    }

    @Test
    @DisplayName("分块结果保留可回溯原文的元数据")
    void keepsTraceableMetadata() {
        TextChunker chunker = new TextChunker(100, 0);

        List<Chunk> chunks = chunker.chunk(doc("短段落"));

        assertThat(chunks).hasSize(1);
        Chunk only = chunks.get(0);
        assertThat(only.docId()).isEqualTo("d1");
        assertThat(only.source()).isEqualTo("d1.md");
        assertThat(only.title()).isEqualTo("测试文档");
    }

    @Test
    @DisplayName("overlap 被限制在目标大小的一半以内，避免无限膨胀")
    void clampsOverlap() {
        TextChunker chunker = new TextChunker(100, 999);

        assertThat(chunker.overlap()).isEqualTo(50);
        assertThat(chunker.targetSize()).isEqualTo(100);
    }
}
