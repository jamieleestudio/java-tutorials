package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** agentscope-channels 示例入口。 */
@SpringBootApplication
public class ChannelsApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(ChannelsApplication.class).run(args);
    }
}