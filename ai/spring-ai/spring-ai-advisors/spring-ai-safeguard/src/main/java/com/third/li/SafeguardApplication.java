package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** spring-ai-safeguard 示例入口。 */
@SpringBootApplication
public class SafeguardApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(SafeguardApplication.class).run(args);
    }
}
