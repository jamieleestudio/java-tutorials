package com.third.li;

import java.util.List;

/**
 * 示例知识库：几条关于 Embabel 的短文（语义检索的语料）。
 */
public final class Corpus {

    private Corpus() {
    }

    public static final List<String> DOCUMENTS = List.of(
            "Action 是 Embabel 中可复用的动作，用 @Action 标注。它的输入来自共享上下文，返回即产出。",
            "Blackboard 是 Agent 的共享上下文，动作之间通过它按类型传递对象。",
            "GOAP 规划器从目标反向规划动作链，因此 Agent 只需要声明目标，不必写死步骤。",
            "UTILITY 规划器不要求有目标，它每轮选择净价值（value - cost）最大的可用动作。",
            "人机协同通过 WaitFor.confirmation 或 WaitFor.formSubmission 暂停流程，等待人工输入后再恢复。",
            "护栏（GuardRail）可以在调用大模型前后校验输入与输出，CRITICAL 级别会阻断本次调用。",
            "LlmReference 把文档或 API 说明注入提示词；内容会全量进入上下文，适合小体量知识。",
            "大知识库应使用 embedding + 向量检索，只把最相关的片段放进提示词以控制 token 成本。",
            "Subagent 把另一个 Agent 包装成工具，实现分层的委派（handoff）模式。",
            "thinking 提取可以把模型的推理过程与最终答案分开返回，要求模型用 <think> 标签包裹推理。");
}
