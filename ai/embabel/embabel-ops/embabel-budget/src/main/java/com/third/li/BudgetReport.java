package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;
import java.util.Map;

/** 一次"带预算的运行"的报告。 */
public record BudgetReport(
        @JsonPropertyDescription("请求内容") String request,
        @JsonPropertyDescription("本次运行的预算配置") Map<String, Object> budget,
        @JsonPropertyDescription("是否达成目标") boolean completed,
        @JsonPropertyDescription("实际执行过的动作") List<String> executedActions,
        @JsonPropertyDescription("是否被提前终止") boolean terminated,
        @JsonPropertyDescription("终止原因") String terminationReason,
        @JsonPropertyDescription("触发终止的策略名") String terminationPolicy,
        @JsonPropertyDescription("终止是否被视为错误") Boolean terminationIsError,
        @JsonPropertyDescription("动作数") int actions,
        @JsonPropertyDescription("累计成本（美元）") double cost,
        @JsonPropertyDescription("累计 token") Integer tokens,
        @JsonPropertyDescription("耗时（毫秒）") long elapsedMillis,
        @JsonPropertyDescription("目标产出（若达成）") String result,
        @JsonPropertyDescription("异常信息（若失败）") String error) {
}
