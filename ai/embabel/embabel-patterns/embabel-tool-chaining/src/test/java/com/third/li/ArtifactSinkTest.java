package com.third.li;

import com.embabel.agent.api.tool.ArtifactSink;
import com.embabel.agent.api.tool.CompositeSink;
import com.embabel.agent.api.tool.ListSink;
import com.embabel.agent.api.tool.Tool;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * artifact sink 的确定性单测——**不需要 API Key、不需要 Spring 上下文**。
 *
 * <p>过滤/转换/多 sink 分发都是纯逻辑，正是最该被测试锁住的部分
 * （README 里那些"实测"结论，现在有测试兜底了）。
 */
class ArtifactSinkTest {

    @Test
    @DisplayName("类过滤 + 谓词过滤 + 转换 + 多 sink 分发")
    void filtersTransformsAndFansOut() {
        ListSink listSink = new ListSink();
        List<String> audit = new ArrayList<>();
        ArtifactSink auditSink = artifact -> audit.add("审计：" + artifact);

        Tool raw = Tool.create("search_orders", "返回一批订单（含干扰项）", args ->
                Tool.Result.withArtifact("命中 3 条", List.of(
                        new Order("A1001", "已支付"),
                        new Order("A1002", "待支付"),
                        "not-an-order")));

        Tool wrapped = Tool.sinkArtifacts(
                raw,
                Order.class,                                  // 类型过滤
                new CompositeSink(listSink, auditSink),       // 多 sink
                order -> "已支付".equals(order.getStatus()),   // 谓词过滤
                order -> order.getId() + "/" + order.getStatus());

        wrapped.call("{}");

        // String 被类型过滤掉，A1002 被谓词过滤掉 → 只剩 A1001，且被转换
        assertThat(listSink.items()).containsExactly("A1001/已支付");
        assertThat(audit).containsExactly("审计：A1001/已支付");
    }

    @Test
    @DisplayName("没有 artifact 时 sink 不会被调用")
    void ignoresPlainTextResults() {
        ListSink listSink = new ListSink();
        Tool raw = Tool.create("plain", "只返回文本", args -> Tool.Result.text("没有任何 artifact"));

        Tool.sinkArtifacts(raw, Order.class, listSink).call("{}");

        assertThat(listSink.items()).isEmpty();
    }

    @Test
    @DisplayName("单个 artifact（非集合）同样能被捕获")
    void capturesSingleArtifact() {
        ListSink listSink = new ListSink();
        Tool raw = Tool.create("one", "返回单个对象", args ->
                Tool.Result.withArtifact("一个订单", new Order("A2001", "已支付")));

        Tool.sinkArtifacts(raw, Order.class, listSink).call("{}");

        // 注意：Order 是普通类、没有 equals，所以按字段断言而不是对象相等
        assertThat(listSink.items()).hasSize(1);
        assertThat(listSink.items().get(0)).isInstanceOf(Order.class);
        assertThat(((Order) listSink.items().get(0)).getId()).isEqualTo("A2001");
    }
}
