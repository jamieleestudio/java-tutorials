package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** context-compaction 示例入口。 */
@SpringBootApplication
public class ContextCompactionApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(ContextCompactionApplication.class).run(args);
    }
}