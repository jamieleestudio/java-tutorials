package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

/** 一次"卡住处理"的观察结果。 */
public record StuckOutcome(
        @JsonPropertyDescription("请求内容") String request,
        @JsonPropertyDescription("是否达成目标") boolean goalAchieved,
        @JsonPropertyDescription("目标产出（若达成）") String result,
        @JsonPropertyDescription("进程最终状态") String status,
        @JsonPropertyDescription("是否发生卡住") boolean stuck,
        @JsonPropertyDescription("StuckHandler 被调用的次数") int handlerCalls,
        @JsonPropertyDescription("每次补救的结论码（REPLAN / NO_RESOLUTION）") List<String> handlerCodes,
        @JsonPropertyDescription("每次补救的说明") List<String> handlerMessages,
        @JsonPropertyDescription("实际执行过的动作") List<String> executedActions,
        @JsonPropertyDescription("异常信息（若失败）") String error) {
}
