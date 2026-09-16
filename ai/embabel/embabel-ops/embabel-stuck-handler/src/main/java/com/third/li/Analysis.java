package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 报告所需的前提：分析要点。
 *
 * <p><b>刻意没有任何动作产出它</b>——所以规划器无法从 {@code UserInput} 走到目标，
 * 进程会进入 STUCK。这正是本模块要演示的场景。
 */
public record Analysis(
        @JsonPropertyDescription("分析要点") String points) {
}
