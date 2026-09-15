package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * GOAP 多步规划示例入口。
 *
 * <p>同一个 {@link WritingAgent} 中的多个 {@code @Action} 通过领域类型串联，
 * 由 Embabel 的 GOAP 规划器自动推导执行顺序。
 */
@SpringBootApplication
public class PlanningApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(PlanningApplication.class).run(args);
    }
}
