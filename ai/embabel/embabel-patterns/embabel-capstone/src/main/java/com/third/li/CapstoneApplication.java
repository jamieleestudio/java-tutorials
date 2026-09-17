package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * 端到端综合示例入口。
 *
 * <p>本模块不引入新概念，只把已有能力串成一个"像产品的东西"：
 * 护栏 → 政策检索 → 工具 → 人工确认 → 预算 → 可观测性。
 */
@SpringBootApplication
public class CapstoneApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(CapstoneApplication.class).run(args);
    }
}
