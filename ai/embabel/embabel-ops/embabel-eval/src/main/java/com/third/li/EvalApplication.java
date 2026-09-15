package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * 评估（eval）示例入口。
 */
@SpringBootApplication
public class EvalApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(EvalApplication.class).run(args);
    }
}
