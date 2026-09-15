package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * Supervisor（主管）模式示例入口。
 */
@SpringBootApplication
public class SupervisorApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(SupervisorApplication.class).run(args);
    }
}
