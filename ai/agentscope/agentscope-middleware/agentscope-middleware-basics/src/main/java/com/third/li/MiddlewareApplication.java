package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** 中间件基础示例入口。 */
@SpringBootApplication
public class MiddlewareApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(MiddlewareApplication.class).run(args);
    }
}