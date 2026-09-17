package com.third.li;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * ChatAgent 单元测试：验证 ChatAgent 能以 DeepSeek 配置构造成功。
 *
 * <p>真正的模型调用需要 API Key，这里只验证对象本身可实例化，
 * 不触发远端调用（HarnessAgent 采用 lazy 构造）。
 * 完整集成测试请在配置好 OPENAI_API_KEY 后运行。
 */
class ChatAgentTest {

    @Test
    void agentCanBeConstructed() {
        ChatAgent chatAgent = new ChatAgent("deepseek-v4-flash", "test-key", "https://api.deepseek.com");
        assertNotNull(chatAgent);
    }
}