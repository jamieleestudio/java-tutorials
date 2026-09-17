package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** mcp 示例入口。 */
@SpringBootApplication
public class McpApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(McpApplication.class).run(args);
    }
}