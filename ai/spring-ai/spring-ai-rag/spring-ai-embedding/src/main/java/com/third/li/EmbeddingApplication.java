package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** spring-ai-embedding 示例入口。 */
@SpringBootApplication
public class EmbeddingApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(EmbeddingApplication.class).run(args);
    }
}
