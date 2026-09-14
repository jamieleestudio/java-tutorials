package com.third.li;

import com.embabel.agent.api.annotation.LlmTool;

import java.time.Instant;

/**
 * 工具集：任何带 {@link LlmTool} 注解的公有方法都会被暴露给大模型。
 *
 * <p>方法参数用 {@code @LlmTool.Param} 提供描述，Embabel 会据此自动生成
 * JSON Schema 供模型决策；方法返回值会被序列化后回传给模型。
 */
public class CalculatorTools {

    @LlmTool(description = "计算两个数字的和")
    public double add(
            @LlmTool.Param(description = "第一个加数") double a,
            @LlmTool.Param(description = "第二个加数") double b) {
        return a + b;
    }

    @LlmTool(description = "计算两个数字的乘积")
    public double multiply(
            @LlmTool.Param(description = "第一个乘数") double a,
            @LlmTool.Param(description = "第二个乘数") double b) {
        return a * b;
    }

    @LlmTool(description = "查询指定城市的当前天气（示例返回模拟数据）")
    public String weather(
            @LlmTool.Param(description = "城市名称，例如：北京") String city) {
        return city + " 当前晴，25°C，湿度 40%（模拟数据）";
    }

    @LlmTool(description = "获取当前 UTC 时间")
    public String now() {
        return Instant.now().toString();
    }
}
