package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** 结构化输出示例入口。 */
@SpringBootApplication
public class StructuredOutputApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(StructuredOutputApplication.class).run(args);
    }
}