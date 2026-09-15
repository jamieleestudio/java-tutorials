package com.third.li;

import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.spi.LlmService;
import com.embabel.common.ai.model.ModelProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 上下文启动测试：**离线**启动 Spring 容器（api-key 用占位符，不会发起任何 LLM 调用）。
 *
 * <p>断言两件事：
 * <ol>
 *   <li>DeepSeek 模型清单被正确加载并注册</li>
 *   <li>Agent 被扫描注册到平台</li>
 * </ol>
 * 这类测试能及早发现配置错误（模型名不匹配、清单未覆盖、Agent 结构非法等）。
 *
 * <p>说明：{@code @SpringBootTest} 默认使用 MOCK web 环境，不绑定端口。
 */
@SpringBootTest
class ContextBootTest {

    @Autowired
    private ModelProvider modelProvider;

    @Autowired
    private AgentPlatform agentPlatform;

    @Test
    void contextLoads_withoutRealApiKey() {
        assertTrue(modelProvider.listModelNames(LlmService.class).contains("deepseek-flash"),
                () -> "registered models: " + modelProvider.listModelNames(LlmService.class));
        assertFalse(agentPlatform.agents().isEmpty(), "expected at least one registered agent");
    }
}
