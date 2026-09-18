package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** spring-ai-tool-context 示例入口。 */
@SpringBootApplication
public class ToolContextApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(ToolContextApplication.class).run(args);
    }
}
