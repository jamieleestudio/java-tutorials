package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** background-tasks 示例入口。 */
@SpringBootApplication
public class BackgroundTasksApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(BackgroundTasksApplication.class).run(args);
    }
}