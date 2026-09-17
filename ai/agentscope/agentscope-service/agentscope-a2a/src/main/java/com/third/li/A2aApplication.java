package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** agentscope-a2a 示例入口。 */
@SpringBootApplication
public class A2aApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(A2aApplication.class).run(args);
    }
}