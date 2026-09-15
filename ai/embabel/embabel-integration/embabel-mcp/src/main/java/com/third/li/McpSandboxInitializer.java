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
 * 准备 MCP 沙箱目录并写入示例文件。
 *
 * <p>目录必须在使用前存在：MCP filesystem server 会把它以 bind mount 挂进容器，
 * 且只会允许访问该目录内的文件。
 */
@Component
public class McpSandboxInitializer {

    private static final Logger log = LoggerFactory.getLogger(McpSandboxInitializer.class);

    private final Path root;

    public McpSandboxInitializer(@Value("${demo.mcp-root}") String root) {
        this.root = Path.of(root);
    }

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(root);

        Path readme = root.resolve("readme.md");
        if (Files.notExists(readme)) {
            Files.writeString(readme, """
                    # MCP 沙箱

                    这个目录通过 MCP filesystem server 暴露给 Agent。
                    Agent 只能读写这里面的文件。

                    示例任务：
                    - 列出目录内容
                    - 读取本文件并总结
                    - 新建 todo.md 并写入三条待办
                    """, StandardCharsets.UTF_8);
        }

        log.info("MCP sandbox ready at {}", root.toAbsolutePath());
    }
}
