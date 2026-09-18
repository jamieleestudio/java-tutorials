package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** spring-ai-streaming 示例入口。 */
@SpringBootApplication
public class StreamingApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(StreamingApplication.class).run(args);
    }
}
