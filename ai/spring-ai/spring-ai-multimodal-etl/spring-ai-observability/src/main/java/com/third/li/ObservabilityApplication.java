package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** spring-ai-observability 示例入口。 */
@SpringBootApplication
public class ObservabilityApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(ObservabilityApplication.class).run(args);
    }
}
