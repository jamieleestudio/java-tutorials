package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** spring-ai-prompt-templates 示例入口。 */
@SpringBootApplication
public class PromptTemplatesApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(PromptTemplatesApplication.class).run(args);
    }
}
