package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** agentscope-tree-of-thoughts 示例入口。 */
@SpringBootApplication
public class TreeOfThoughtsApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(TreeOfThoughtsApplication.class).run(args);
    }
}
