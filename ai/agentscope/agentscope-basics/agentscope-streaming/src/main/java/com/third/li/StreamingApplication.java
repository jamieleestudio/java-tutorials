package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** 流式输出示例入口。 */
@SpringBootApplication
public class StreamingApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(StreamingApplication.class).run(args);
    }
}