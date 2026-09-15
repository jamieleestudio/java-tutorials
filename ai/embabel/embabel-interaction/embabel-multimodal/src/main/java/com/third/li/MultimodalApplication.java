package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * 多模态（图像理解）示例入口。
 */
@SpringBootApplication
public class MultimodalApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(MultimodalApplication.class).run(args);
    }
}
