package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** agentscope-programmatic-dsl 示例入口。 */
@SpringBootApplication
public class ProgrammaticDslApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(ProgrammaticDslApplication.class).run(args);
    }
}
