package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** coding-agent 示例入口。 */
@SpringBootApplication
public class CodingAgentApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(CodingAgentApplication.class).run(args);
    }
}