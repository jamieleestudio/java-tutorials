package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** 工具调用示例入口。 */
@SpringBootApplication
public class ToolsApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(ToolsApplication.class).run(args);
    }
}