package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * 文档摄入流水线示例入口。
 */
@SpringBootApplication
public class DocumentIngestApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(DocumentIngestApplication.class).run(args);
    }
}
