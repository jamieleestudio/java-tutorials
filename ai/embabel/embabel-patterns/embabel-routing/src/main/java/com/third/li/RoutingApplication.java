package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * Routing（路由）示例入口。
 */
@SpringBootApplication
public class RoutingApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(RoutingApplication.class).run(args);
    }
}
