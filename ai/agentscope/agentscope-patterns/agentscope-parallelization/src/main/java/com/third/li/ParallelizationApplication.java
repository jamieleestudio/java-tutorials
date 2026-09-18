package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** agentscope-parallelization 示例入口。 */
@SpringBootApplication
public class ParallelizationApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(ParallelizationApplication.class).run(args);
    }
}
