package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * 最小 Web UI 示例入口。启动后直接打开 <http://localhost:8943/>。
 */
@SpringBootApplication
public class WebUiApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(WebUiApplication.class).run(args);
    }
}
