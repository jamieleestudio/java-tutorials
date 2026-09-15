package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

/** 编排者动态拆解出的子任务（数量由模型决定）。 */
public record Subtasks(
        @JsonPropertyDescription("子任务列表，每个一句话") List<String> subtasks) {
}
