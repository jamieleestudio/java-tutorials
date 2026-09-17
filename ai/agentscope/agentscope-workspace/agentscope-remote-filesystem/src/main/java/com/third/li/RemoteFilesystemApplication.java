package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** agentscope-remote-filesystem 示例入口。 */
@SpringBootApplication
public class RemoteFilesystemApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(RemoteFilesystemApplication.class).run(args);
    }
}