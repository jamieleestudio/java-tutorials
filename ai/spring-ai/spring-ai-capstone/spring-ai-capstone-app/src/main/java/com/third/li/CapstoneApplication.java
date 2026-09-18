package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** spring-ai-capstone 示例入口。 */
@SpringBootApplication
public class CapstoneApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(CapstoneApplication.class).run(args);
    }
}
