package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * 规划器类型（GOAP / UTILITY）示例入口。
 */
@SpringBootApplication
public class PlannerTypesApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(PlannerTypesApplication.class).run(args);
    }
}
