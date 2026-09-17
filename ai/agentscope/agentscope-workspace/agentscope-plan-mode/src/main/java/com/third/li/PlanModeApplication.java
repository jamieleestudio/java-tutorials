package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** agentscope-plan-mode 示例入口。 */
@SpringBootApplication
public class PlanModeApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(PlanModeApplication.class).run(args);
    }
}