package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * 工具安全实践示例入口。
 */
@SpringBootApplication
public class SecureToolsApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(SecureToolsApplication.class).run(args);
    }
}
