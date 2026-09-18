package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** spring-ai-multimodal 示例入口。 */
@SpringBootApplication
public class MultimodalApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(MultimodalApplication.class).run(args);
    }
}
