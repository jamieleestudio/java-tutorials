package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * 多 Agent 辩论示例入口。
 */
@SpringBootApplication
public class DebateApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(DebateApplication.class).run(args);
    }
}
