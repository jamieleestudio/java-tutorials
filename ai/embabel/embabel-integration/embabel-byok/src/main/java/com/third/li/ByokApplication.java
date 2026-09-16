package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * 多租户 / BYOK 示例入口。
 */
@SpringBootApplication
public class ByokApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(ByokApplication.class).run(args);
    }
}
