package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** spring-ai-state-machine 示例入口。 */
@SpringBootApplication
public class StateMachineApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(StateMachineApplication.class).run(args);
    }
}
