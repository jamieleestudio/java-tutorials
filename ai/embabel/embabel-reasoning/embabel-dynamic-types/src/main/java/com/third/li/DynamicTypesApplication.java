package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * 运行时类型（DynamicType）示例入口。
 *
 * <p>本模块**不需要 LLM**：它演示的是"类型即 schema"这一层，纯对象构造与渲染。
 */
@SpringBootApplication
public class DynamicTypesApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(DynamicTypesApplication.class).run(args);
    }
}
