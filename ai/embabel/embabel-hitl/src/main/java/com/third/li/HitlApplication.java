package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * 人机协同（HITL）示例入口。
 */
@SpringBootApplication
public class HitlApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(HitlApplication.class).run(args);
    }
}
