package com.third.li;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * **段落感知的分块器**——这是 RAG 里最容易被低估、也最影响效果的一步。
 *
 * <p>做法：
 * <ol>
 *   <li>按**空行**切段（保留段落语义，而不是按字符硬切）</li>
 *   <li>贪心把段落拼到接近 {@code targetSize} 再落一块</li>
 *   <li>单段本身就超长时，对它做硬切（否则一块会远超目标大小）</li>
 *   <li>相邻块之间加 {@code overlap} 个字符的**重叠**，避免"答案正好被切在边界上"</li>
 * </ol>
 *
 * <p>为什么不能只按固定长度切：中文/代码/表格被硬切后会丢失语义，
 * 检索命中的块里往往缺少完整句子，模型就没法答对。
 *
 * <p>参数取舍：块越大 → 上下文越完整但噪声越多、token 越贵；
 * 块越小 → 召回更准但可能缺上下文。{@code 300/60} 是个适合演示的起点。
 */
@Component
public class TextChunker {

    private final int targetSize;
    private final int overlap;

    public TextChunker(
            @Value("${app.ingest.chunk-size:300}") int targetSize,
            @Value("${app.ingest.chunk-overlap:60}") int overlap) {
        this.targetSize = Math.max(50, targetSize);
        this.overlap = Math.max(0, Math.min(overlap, this.targetSize / 2));
    }

    public int targetSize() {
        return targetSize;
    }

    public int overlap() {
        return overlap;
    }

    /** 把一份文档切成若干块。 */
    public List<Chunk> chunk(RawDocument document) {
        List<String> pieces = new ArrayList<>();
        StringBuilder buffer = new StringBuilder();

        for (String paragraph : splitParagraphs(document.text())) {
            for (String piece : hardSplitIfTooLong(paragraph)) {
                if (buffer.length() > 0 && buffer.length() + piece.length() + 1 > targetSize) {
                    pieces.add(buffer.toString());
                    buffer.setLength(0);
                }
                if (buffer.length() > 0) {
                    buffer.append('\n');
                }
                buffer.append(piece);
            }
        }
        if (buffer.length() > 0) {
            pieces.add(buffer.toString());
        }

        List<Chunk> chunks = new ArrayList<>();
        String previous = null;
        for (int i = 0; i < pieces.size(); i++) {
            String text = pieces.get(i);
            if (previous != null && overlap > 0) {
                // 把上一块的尾部接进来，保证跨块语义连续
                String tail = previous.length() <= overlap
                        ? previous
                        : previous.substring(previous.length() - overlap);
                text = tail + "\n" + text;
            }
            chunks.add(new Chunk(
                    Chunk.idOf(document.docId(), i),
                    document.docId(),
                    document.source(),
                    document.title(),
                    i,
                    text));
            previous = pieces.get(i);
        }
        return chunks;
    }

    private List<String> splitParagraphs(String text) {
        List<String> paragraphs = new ArrayList<>();
        for (String block : text.split("\n\\s*\n")) {
            String stripped = block.strip();
            if (!stripped.isEmpty()) {
                paragraphs.add(stripped);
            }
        }
        return paragraphs;
    }

    /** 单段超过目标大小 2 倍就硬切，避免"一个巨块"。 */
    private List<String> hardSplitIfTooLong(String paragraph) {
        if (paragraph.length() <= targetSize * 2) {
            return List.of(paragraph);
        }
        List<String> parts = new ArrayList<>();
        for (int start = 0; start < paragraph.length(); start += targetSize) {
            parts.add(paragraph.substring(start, Math.min(start + targetSize, paragraph.length())));
        }
        return parts;
    }
}
