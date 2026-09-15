package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

/** 所有 worker 的结果。 */
public record WorkerResults(
        @JsonPropertyDescription("各子任务的结论") List<WorkerResult> results) {
}
