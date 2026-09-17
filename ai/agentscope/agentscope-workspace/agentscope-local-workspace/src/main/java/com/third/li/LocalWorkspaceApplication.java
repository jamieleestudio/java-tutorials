package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** agentscope-local-workspace 示例入口。 */
@SpringBootApplication
public class LocalWorkspaceApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(LocalWorkspaceApplication.class).run(args);
    }
}