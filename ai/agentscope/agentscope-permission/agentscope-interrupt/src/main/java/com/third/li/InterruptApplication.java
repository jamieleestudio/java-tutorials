package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** interrupt 示例入口。 */
@SpringBootApplication
public class InterruptApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(InterruptApplication.class).run(args);
    }
}