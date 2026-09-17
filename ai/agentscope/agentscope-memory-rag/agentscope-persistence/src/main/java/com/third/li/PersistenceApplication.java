package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** persistence 示例入口。 */
@SpringBootApplication
public class PersistenceApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(PersistenceApplication.class).run(args);
    }
}