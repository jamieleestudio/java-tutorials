package com.third.li;

import com.embabel.agent.api.tool.Tool;
import com.embabel.agent.api.tool.ToolCallContext;
import com.embabel.agent.core.ProcessOptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 租户隔离的确定性单测——**不需要 API Key、不需要 Spring 上下文**。
 *
 * <p>这是本模块最重要的一条断言：**隔离必须显式实现**。
 * 两个工具读同一份数据，只有读了 {@code ToolCallContext} 的那个才做了隔离。
 */
class OrderToolsTest {

    private final OrderTools tools = new OrderTools();

    /** 用 ProcessOptions 构造 ToolCallContext（框架没有暴露公开构造器，这是最简路径）。 */
    private ToolCallContext context(String tenantId) {
        return ProcessOptions.DEFAULT.withToolCallContext(Map.of("tenantId", tenantId)).getToolCallContext();
    }

    private String content(Tool.Result result) {
        return result instanceof Tool.Result.Text text ? text.getContent() : result.toString();
    }

    @Test
    @DisplayName("读上下文的工具只返回当前租户的数据")
    void scopedToolFiltersByTenant() {
        String acme = content(tools.tenantScopedQuery().call("{}", context("acme")));
        String globex = content(tools.tenantScopedQuery().call("{}", context("globex")));

        assertThat(acme).contains("A1001").contains("A1003").doesNotContain("A1002");
        assertThat(globex).contains("A1002").doesNotContain("A1001");
    }

    @Test
    @DisplayName("不读上下文的工具会泄露其它租户数据（反面教材）")
    void leakyToolLeaks() {
        String all = content(tools.leakyQuery().call("{}"));

        assertThat(all).contains("A1001").contains("A1002").contains("A1003");
    }

    @Test
    @DisplayName("没有租户上下文时查不到任何数据（失败关闭，而不是失败开放）")
    void missingTenantYieldsNothing() {
        String unknown = content(tools.tenantScopedQuery().call("{}", ToolCallContext.EMPTY));

        assertThat(unknown).contains("unknown").doesNotContain("A1001").doesNotContain("A1002");
    }
}
