package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * 预算与熔断示例入口。
 */
@SpringBootApplication
public class BudgetApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(BudgetApplication.class).run(args);
    }
}
