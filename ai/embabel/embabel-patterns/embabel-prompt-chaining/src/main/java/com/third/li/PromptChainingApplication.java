package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * Prompt chaining（链式提示 + 关卡）示例入口。
 */
@SpringBootApplication
public class PromptChainingApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(PromptChainingApplication.class).run(args);
    }
}
