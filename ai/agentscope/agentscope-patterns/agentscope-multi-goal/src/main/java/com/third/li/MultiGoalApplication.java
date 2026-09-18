package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** agentscope-multi-goal 示例入口。 */
@SpringBootApplication
public class MultiGoalApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(MultiGoalApplication.class).run(args);
    }
}
