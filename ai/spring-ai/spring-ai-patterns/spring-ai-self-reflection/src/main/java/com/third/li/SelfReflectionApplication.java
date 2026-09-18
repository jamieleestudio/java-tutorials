package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** spring-ai-self-reflection 示例入口。 */
@SpringBootApplication
public class SelfReflectionApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(SelfReflectionApplication.class).run(args);
    }
}
