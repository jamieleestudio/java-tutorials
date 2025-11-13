package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * Hello world!
 *
 */
@SpringBootApplication
public class DeepSeekApplication {

    public static void main( String[] args ) {
        new SpringApplicationBuilder(DeepSeekApplication.class).run(args);
    }
}
