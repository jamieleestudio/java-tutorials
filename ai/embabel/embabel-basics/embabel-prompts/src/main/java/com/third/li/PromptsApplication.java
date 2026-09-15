package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * 提示词工程示例入口。
 */
@SpringBootApplication
public class PromptsApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(PromptsApplication.class).run(args);
    }
}
