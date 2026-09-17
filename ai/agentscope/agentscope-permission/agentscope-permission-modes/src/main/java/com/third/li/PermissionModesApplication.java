package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** agentscope-permission-modes 示例入口。 */
@SpringBootApplication
public class PermissionModesApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(PermissionModesApplication.class).run(args);
    }
}