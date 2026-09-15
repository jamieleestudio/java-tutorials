package com.third.li;

import java.util.List;

/**
 * 评估数据集（示例）：每条 = 问题 + 判定标准。
 *
 * <p>真实项目里这些用例通常来自：线上失败案例、需求文档、领域专家标注。
 */
public final class Dataset {

    private Dataset() {
    }

    public record EvalCase(String id, String question, String criterion) {
    }

    public static final List<EvalCase> CASES = List.of(
            new EvalCase("case-1",
                    "Action 和 Goal 在 Embabel 里有什么区别？",
                    "必须说明 Action 是可复用的动作、Goal 是目标；两者概念清晰、无混淆"),
            new EvalCase("case-2",
                    "GOAP 规划器和 UTILITY 规划器有什么不同？",
                    "必须指出 GOAP 从目标反向规划、需要目标；UTILITY 按价值选择动作、不要求目标"),
            new EvalCase("case-3",
                    "怎么控制大知识库的 token 成本？",
                    "必须提到用嵌入/向量检索只把相关片段放进提示词"),
            new EvalCase("case-4",
                    "Agent 调用工具失败时该怎么办？",
                    "必须提到错误恢复/重试/重规划等机制，而不是直接放弃"));
}
