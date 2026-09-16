package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * 思维树（Tree of Thoughts）示例入口。
 */
@SpringBootApplication
public class TotApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(TotApplication.class).run(args);
    }
}
