package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** capstone 示例入口。 */
@SpringBootApplication
public class CapstoneE2eApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(CapstoneE2eApplication.class).run(args);
    }
}