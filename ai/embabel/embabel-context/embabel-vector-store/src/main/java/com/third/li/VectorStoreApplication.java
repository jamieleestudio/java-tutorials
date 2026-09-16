package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * pgvector 向量库示例入口。
 */
@SpringBootApplication
public class VectorStoreApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(VectorStoreApplication.class).run(args);
    }
}
