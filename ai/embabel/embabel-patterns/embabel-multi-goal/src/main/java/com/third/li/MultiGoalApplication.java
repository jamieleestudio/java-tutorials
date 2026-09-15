package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * 多目标（自动选择）示例入口。
 */
@SpringBootApplication
public class MultiGoalApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(MultiGoalApplication.class).run(args);
    }
}
