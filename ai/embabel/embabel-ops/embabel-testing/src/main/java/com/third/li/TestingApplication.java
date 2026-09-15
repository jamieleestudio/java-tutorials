package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * 测试示例入口。
 *
 * <p>本模块的重点不是运行服务，而是演示**无需 API Key** 的三种测试方式：
 * <ol>
 *   <li>规划校验：用 {@code GoapPathToCompletionValidator} 验证目标可达</li>
 *   <li>动作单测：用 Mockito 模拟 {@code Ai}/{@code PromptRunner}</li>
 *   <li>上下文启动：{@code @SpringBootTest} 离线启动并断言模型注册</li>
 * </ol>
 * 运行：{@code mvn -pl :embabel-testing test}
 */
@SpringBootApplication
public class TestingApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(TestingApplication.class).run(args);
    }
}
