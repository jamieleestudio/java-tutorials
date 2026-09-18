package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** spring-ai-etl 示例入口。 */
@SpringBootApplication
public class EtlApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(EtlApplication.class).run(args);
    }
}
