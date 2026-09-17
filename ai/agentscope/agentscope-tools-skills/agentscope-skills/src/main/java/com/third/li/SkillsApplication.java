package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** agentscope-skills 示例入口。 */
@SpringBootApplication
public class SkillsApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(SkillsApplication.class).run(args);
    }
}