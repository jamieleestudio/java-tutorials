package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** agentscope-playbook 示例入口。 */
@SpringBootApplication
public class PlaybookApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(PlaybookApplication.class).run(args);
    }
}
