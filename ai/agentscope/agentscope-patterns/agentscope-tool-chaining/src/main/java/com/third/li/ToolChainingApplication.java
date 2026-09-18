package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** agentscope-tool-chaining 示例入口。 */
@SpringBootApplication
public class ToolChainingApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(ToolChainingApplication.class).run(args);
    }
}
