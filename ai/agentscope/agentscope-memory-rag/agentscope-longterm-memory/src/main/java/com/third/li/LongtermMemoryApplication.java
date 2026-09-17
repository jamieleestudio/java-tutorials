package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** longterm-memory 示例入口。 */
@SpringBootApplication
public class LongtermMemoryApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(LongtermMemoryApplication.class).run(args);
    }
}