package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * 反应式触发（trigger）示例入口。
 */
@SpringBootApplication
public class TriggerApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(TriggerApplication.class).run(args);
    }
}
