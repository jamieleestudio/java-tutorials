package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * StuckHandler 示例入口。
 */
@SpringBootApplication
public class StuckHandlerApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(StuckHandlerApplication.class).run(args);
    }
}
