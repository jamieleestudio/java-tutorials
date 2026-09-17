package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** agentscope-rag-backends 示例入口。 */
@SpringBootApplication
public class RagBackendsApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(RagBackendsApplication.class).run(args);
    }
}