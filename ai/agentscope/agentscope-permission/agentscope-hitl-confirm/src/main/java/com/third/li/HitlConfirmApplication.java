package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** agentscope-hitl-confirm 示例入口。 */
@SpringBootApplication
public class HitlConfirmApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(HitlConfirmApplication.class).run(args);
    }
}