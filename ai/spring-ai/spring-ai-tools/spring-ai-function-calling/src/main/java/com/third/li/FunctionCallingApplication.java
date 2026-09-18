package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** spring-ai-function-calling 示例入口。 */
@SpringBootApplication
public class FunctionCallingApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(FunctionCallingApplication.class).run(args);
    }
}
