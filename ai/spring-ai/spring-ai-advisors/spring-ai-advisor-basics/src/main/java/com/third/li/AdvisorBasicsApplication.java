package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** spring-ai-advisor-basics 示例入口。 */
@SpringBootApplication
public class AdvisorBasicsApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(AdvisorBasicsApplication.class).run(args);
    }
}
