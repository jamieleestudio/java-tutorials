package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** agentscope-scheduler 示例入口。 */
@SpringBootApplication
public class SchedulerApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(SchedulerApplication.class).run(args);
    }
}