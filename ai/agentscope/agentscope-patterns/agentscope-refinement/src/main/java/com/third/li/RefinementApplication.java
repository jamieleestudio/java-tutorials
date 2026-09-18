package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** agentscope-refinement 示例入口。 */
@SpringBootApplication
public class RefinementApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(RefinementApplication.class).run(args);
    }
}
