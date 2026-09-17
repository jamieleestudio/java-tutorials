package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** agentscope-coding-tools 示例入口。 */
@SpringBootApplication
public class CodingToolsApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(CodingToolsApplication.class).run(args);
    }
}