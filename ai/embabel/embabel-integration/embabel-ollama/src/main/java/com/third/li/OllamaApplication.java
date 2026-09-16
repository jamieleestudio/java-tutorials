package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * 本地模型（Ollama）示例入口。
 */
@SpringBootApplication
public class OllamaApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(OllamaApplication.class).run(args);
    }
}
