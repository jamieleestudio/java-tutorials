package com.third.li;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 准备沙箱目录：文件工具只能在该目录内操作，避免影响宿主机其他文件。
 *
 * <p>启动时创建目录并写入一个示例文件，便于演示"读取并总结"。
 */
@Component
public class SandboxInitializer {

    private static final Logger log = LoggerFactory.getLogger(SandboxInitializer.class);

    private final Path sandbox;

    public SandboxInitializer(@Value("${demo.sandbox-dir}") String sandboxDir) {
        this.sandbox = Path.of(sandboxDir);
    }

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(sandbox);

        Path notes = sandbox.resolve("notes.md");
        if (Files.notExists(notes)) {
            Files.writeString(notes, """
                    # 沙箱示例笔记

                    - 这是文件工具示例的初始文件。
                    - 文件工具的所有路径都相对于沙箱根目录解析。
                    - 可以用 listFiles / readFile / writeFile / findFiles 等工具操作。
                    """, StandardCharsets.UTF_8);
        }

        log.info("File tools sandbox ready at {}", sandbox.toAbsolutePath());
    }
}
