package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * MCP server 示例入口（把 Agent 暴露给 MCP 客户端）。
 */
@SpringBootApplication
public class McpServerApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(McpServerApplication.class).run(args);
    }
}
