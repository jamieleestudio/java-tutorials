package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/** 单个 worker 的执行结果。 */
public record WorkerResult(
        @JsonPropertyDescription("子任务") String subtask,
        @JsonPropertyDescription("该子任务的结论") String finding) {
}
