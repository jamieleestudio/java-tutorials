package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** agentscope-replanning 示例入口。 */
@SpringBootApplication
public class ReplanningApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(ReplanningApplication.class).run(args);
    }
}
