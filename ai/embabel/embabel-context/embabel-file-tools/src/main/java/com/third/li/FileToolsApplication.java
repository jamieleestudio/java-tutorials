package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * 文件工具（沙箱）示例入口。
 */
@SpringBootApplication
public class FileToolsApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(FileToolsApplication.class).run(args);
    }
}
