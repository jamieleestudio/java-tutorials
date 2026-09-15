package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * thinking（推理过程提取）示例入口。
 */
@SpringBootApplication
public class ThinkingApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(ThinkingApplication.class).run(args);
    }
}
