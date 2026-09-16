package com.third.li;

import com.embabel.agent.api.annotation.LlmTool;

/**
 * 领域对象：订单。
 *
 * <p>关键点：**它自己带 `@LlmTool` 方法**。这些方法一开始对模型是**不可见**的——
 * 只有当某个工具返回了一个 `Order` 实例（并且该实例被落到黑板上）之后，
 * 通过 {@code withToolChainingFrom(Order.class)} 声明，它们才会**解锁**。
 *
 * <p>这就是"工具链式展开"：不需要一开始就把所有工具塞进提示词，
 * 而是让**领域对象出现**这件事本身成为"解锁条件"。
 */
public class Order {

    private final String id;
    private final String status;
    private double discount;

    public Order(String id, String status) {
        this.id = id;
        this.status = status;
    }

    @LlmTool(description = "给这个订单应用折扣。rate 是折扣率，例如 0.9 表示打 9 折")
    public String applyDiscount(
            @LlmTool.Param(description = "折扣率，0~1 之间的小数") double rate) {
        this.discount = rate;
        return "订单 %s 已应用 %.0f%% 折扣".formatted(id, rate * 100);
    }

    @LlmTool(description = "查询这个订单的当前状态（含已应用的折扣）")
    public String currentStatus() {
        return "订单 %s：状态=%s，折扣=%.2f".formatted(id, status, discount);
    }

    public String getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public double getDiscount() {
        return discount;
    }
}
