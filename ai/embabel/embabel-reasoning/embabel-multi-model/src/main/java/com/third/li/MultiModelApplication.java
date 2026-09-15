package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * 多模型（角色映射与回退）示例入口。
 */
@SpringBootApplication
public class MultiModelApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(MultiModelApplication.class).run(args);
    }
}
