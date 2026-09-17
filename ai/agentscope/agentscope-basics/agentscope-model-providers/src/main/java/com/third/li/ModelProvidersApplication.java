package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** 多模型 Provider 示例入口。 */
@SpringBootApplication
public class ModelProvidersApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(ModelProvidersApplication.class).run(args);
    }
}