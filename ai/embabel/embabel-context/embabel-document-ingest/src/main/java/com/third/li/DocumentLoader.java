package com.third.li;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;

/**
 * 把磁盘上的文件读成 {@link RawDocument}。
 *
 * <p>支持三种格式：
 * <ul>
 *   <li>{@code .md} / {@code .txt} —— 直接读文本</li>
 *   <li>{@code .pdf} —— 用 <b>PDFBox</b> 抽文本（{@code Loader.loadPDF} + {@code PDFTextStripper}）</li>
 * </ul>
 *
 * <p>为什么直接用 PDFBox 而不是 Spring AI 的 {@code PagePdfDocumentReader}：
 * "抽文本"本身只有两步（打开 + stripper），直连少一层抽象，
 * 也避免为了一个 reader 引入整套 Spring AI 文档 API。
 * 如果你的项目已经用了 Spring AI，用它的 reader 也可以，后续分块逻辑完全一样。
 *
 * <p><b>PDF 的现实问题</b>：扫描件（图片型 PDF）抽不出文本，需要 OCR；
 * 双栏排版、表格、页眉页脚也会污染文本。生产上通常还要清洗（去页眉、合并断行）。
 */
@Component
public class DocumentLoader {

    private static final Logger log = LoggerFactory.getLogger(DocumentLoader.class);

    private static final java.util.Set<String> TEXT_EXTENSIONS = java.util.Set.of("md", "txt", "markdown");

    /** 读取一个文件；不支持的扩展名返回 empty。 */
    public Optional<RawDocument> load(Path file, Path root) {
        String name = file.getFileName().toString();
        String extension = extensionOf(name);
        String relative = root.relativize(file).toString().replace('\\', '/');

        String text;
        try {
            if (TEXT_EXTENSIONS.contains(extension)) {
                text = Files.readString(file, StandardCharsets.UTF_8);
            } else if ("pdf".equals(extension)) {
                text = extractPdf(file);
            } else {
                return Optional.empty();
            }
        } catch (IOException e) {
            log.warn("读取失败，跳过 {}：{}", relative, e.getMessage());
            return Optional.empty();
        }

        if (text.isBlank()) {
            log.warn("抽出文本为空，跳过 {}（扫描件 PDF 需要 OCR）", relative);
            return Optional.empty();
        }

        return Optional.of(new RawDocument(
                name,
                relative,
                titleOf(text, name),
                normalize(text),
                sha256(text)));
    }

    private String extractPdf(Path file) throws IOException {
        try (PDDocument document = Loader.loadPDF(file.toFile())) {
            if (document.isEncrypted()) {
                throw new IOException("PDF 已加密");
            }
            return new PDFTextStripper().getText(document);
        }
    }

    /** 标题：优先第一个 Markdown 标题，否则用文件名（去扩展名）。 */
    private String titleOf(String text, String fileName) {
        for (String line : text.lines().toList()) {
            String trimmed = line.strip();
            if (trimmed.startsWith("#")) {
                String heading = trimmed.replaceAll("^#+\\s*", "").strip();
                if (!heading.isEmpty()) {
                    return heading;
                }
            }
        }
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(0, dot) : fileName;
    }

    /** 统一换行、去掉行尾空白，避免"内容没变但哈希变了"导致无谓的重新嵌入。 */
    private String normalize(String text) {
        return text.replace("\r\n", "\n").replace("\r", "\n")
                .lines()
                .map(String::stripTrailing)
                .reduce((a, b) -> a + "\n" + b)
                .orElse("");
    }

    private String sha256(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(text.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("计算哈希失败", e);
        }
    }

    private String extensionOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
