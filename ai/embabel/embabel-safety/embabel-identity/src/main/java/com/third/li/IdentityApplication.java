package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * 身份与请求级元数据示例入口。
 *
 * <p>本模块**不需要 LLM**：身份与上下文的透传是确定性的，直接调用工具即可验证。
 */
@SpringBootApplication
public class IdentityApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(IdentityApplication.class).run(args);
    }
}
