package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** agentscope-skill-curator 示例入口。 */
@SpringBootApplication
public class SkillCuratorApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(SkillCuratorApplication.class).run(args);
    }
}