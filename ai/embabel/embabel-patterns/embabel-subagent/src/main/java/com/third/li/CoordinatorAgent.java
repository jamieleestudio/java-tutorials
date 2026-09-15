package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.tool.Subagent;
import com.embabel.agent.api.tool.Tool;
import com.embabel.agent.domain.io.UserInput;

/**
 * 调度 Agent（主 Agent）：把翻译这类专门任务委派给 {@link TranslatorAgent}。
 *
 * <p>{@link Subagent#ofClass(Class)} 把另一个 Agent 包装成工具挂到当前 PromptRunner 上，
 * 由模型决定何时调用；子 Agent 输入输出类型都由 {@code consuming(...)} 声明，
 * Embabel 会据此生成工具 JSON Schema。这是典型的 handoff / 分层 Agent 模式。
 */
@Agent(description = "调度 Agent：把翻译任务委派给 Translator 子 Agent")
public class CoordinatorAgent {

    @Action(description = "处理用户请求，必要时调用子 Agent")
    @AchievesGoal(description = "返回处理结果")
    public ChatReply handle(UserInput userInput, Ai ai) {
        Tool translator = Subagent.ofClass(TranslatorAgent.class)
                .consuming(TranslationRequest.class);

        String answer = ai.withDefaultLlm()
                .withTool(translator)
                .withId("coordinator")
                .generateText("""
                        你可以调用 TranslatorAgent 工具把文本翻译成指定语言。
                        如果用户没有指明目标语言，默认翻译成英文。
                        用户请求：%s
                        """.formatted(userInput.getContent()));
        return new ChatReply(answer);
    }
}
