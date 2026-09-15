package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * 嵌入与语义检索示例入口。
 */
@SpringBootApplication
public class EmbeddingsApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(EmbeddingsApplication.class).run(args);
    }
}
