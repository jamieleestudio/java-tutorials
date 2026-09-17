package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** agentscope-custom-budget 示例入口。 */
@SpringBootApplication
public class CustomBudgetApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(CustomBudgetApplication.class).run(args);
    }
}