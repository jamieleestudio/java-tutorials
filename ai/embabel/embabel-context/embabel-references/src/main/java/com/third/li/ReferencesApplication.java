package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * LlmReference（轻量 RAG）示例入口。
 */
@SpringBootApplication
public class ReferencesApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(ReferencesApplication.class).run(args);
    }
}
