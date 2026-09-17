package com.third.li;

import com.embabel.agent.api.tool.Tool;
import com.embabel.agent.api.tool.ToolCallContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据访问工具：演示 {@link ToolCallContext}（**请求级元数据**）如何透传到工具。
 *
 * <p>为什么需要它：Agent 往往由"用户请求"驱动，但工具需要知道**这个请求属于谁**——
 * 租户 ID、用户 ID、请求 ID、下游 token。把这些塞进提示词既不安全也不可靠；
 * 正确做法是走**带外元数据**（out-of-band），框架会把它传给每一个工具
 * （对 MCP 工具还会桥接到 Spring AI 的 ToolContext → MCP 的 McpMeta）。
 *
 * <p>本模块刻意给出**两个工具**做对照：
 * <ul>
 *   <li>{@link #leakyQuery()} —— 不读上下文，返回**所有租户**的数据（反面教材）</li>
 *   <li>{@link #tenantScopedQuery()} —— 读上下文，只返回当前租户的数据</li>
 * </ul>
 * 结论：**隔离必须显式实现**——框架负责把元数据送到工具手上，但"用不用它"是你的代码决定的。
 */
@Component
public class OrderTools {

    /** 演示数据：两个租户。 */
    private final List<Order> allOrders = List.of(
            new Order("A1001", "acme", 500.0),
            new Order("A1002", "globex", 800.0),
            new Order("A1003", "acme", 1200.0));

    /** 反面教材：不读上下文，会把别的租户数据也返回。 */
    public Tool leakyQuery() {
        return Tool.create("listAllOrders", "列出全部订单（不区分租户）", args ->
                Tool.Result.text(allOrders.stream().map(Order::id).collect(Collectors.joining(", "))));
    }

    /**
     * 正确做法：从 {@link ToolCallContext} 读出租户，只返回该租户的数据。
     *
     * <p>{@code Tool.create(...)} 的 handler 只能拿到入参字符串，**拿不到上下文**，
     * 所以这里直接实现 {@link Tool}，覆写带 {@code ToolCallContext} 的 {@code call}。
     * （框架内部的 {@code ContextAwareFunctionalTool} 是包级可见的，外部代码用不了。）
     */
    public Tool tenantScopedQuery() {
        Tool base = Tool.create("listMyOrders", "列出当前租户的订单", args -> Tool.Result.text(""));
        return new Tool() {
            @Override
            public Tool.Definition getDefinition() {
                return base.getDefinition();
            }

            @Override
            public Tool.Metadata getMetadata() {
                return base.getMetadata();
            }

            @Override
            public Tool.Result call(String input) {
                return call(input, ToolCallContext.EMPTY);
            }

            @Override
            public Tool.Result call(String input, ToolCallContext context) {
                String tenantId = context.getOrDefault("tenantId", "unknown");
                String requestId = context.getOrDefault("requestId", "-");
                List<String> mine = allOrders.stream()
                        .filter(order -> order.tenantId().equals(tenantId))
                        .map(order -> "%s(%.0f)".formatted(order.id(), order.amount()))
                        .toList();
                return Tool.Result.text("租户 %s 的订单：%s（requestId=%s）"
                        .formatted(tenantId, mine, requestId));
            }
        };
    }
}
