package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * 自省工具（agentic tools）示例入口。
 */
@SpringBootApplication
public class AgenticToolsApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(AgenticToolsApplication.class).run(args);
    }
}
