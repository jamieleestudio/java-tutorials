package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * 自主 Agent 示例入口。
 */
@SpringBootApplication
public class AutonomousApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(AutonomousApplication.class).run(args);
    }
}
