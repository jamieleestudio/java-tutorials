package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** agentscope-permission-rules 示例入口。 */
@SpringBootApplication
public class PermissionRulesApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(PermissionRulesApplication.class).run(args);
    }
}