package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * 跨会话长期记忆示例入口。
 */
@SpringBootApplication
public class MemoryApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(MemoryApplication.class).run(args);
    }
}
