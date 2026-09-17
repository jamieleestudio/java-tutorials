package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** docker-sandbox 示例入口。 */
@SpringBootApplication
public class DockerSandboxApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(DockerSandboxApplication.class).run(args);
    }
}