package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * 工具级 HITL 示例入口。
 *
 * <p>本模块**不需要 LLM**：整条链路是确定性的（直接调用被 HITL 包装的工具），
 * 所以不需要 API Key 也能跑通。
 */
@SpringBootApplication
public class HitlAdvancedApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(HitlAdvancedApplication.class).run(args);
    }
}
