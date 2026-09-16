package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * 工具链式展开示例入口。
 */
@SpringBootApplication
public class ToolChainingApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(ToolChainingApplication.class).run(args);
    }
}
