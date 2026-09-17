package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** agentscope-rag 示例入口。 */
@SpringBootApplication
public class RagApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(RagApplication.class).run(args);
    }
}