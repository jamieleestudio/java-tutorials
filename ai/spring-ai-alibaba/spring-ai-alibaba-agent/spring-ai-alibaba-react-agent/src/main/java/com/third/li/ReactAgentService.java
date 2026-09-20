package com.third.li;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.modelcalllimit.ModelCallLimitHook;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

/**
 * ReactAgent 基础：Builder 装配 + call 调用 + Hook 体系。
 *
 * <p>Agent Framework（agent-framework 模块）在 Graph 之上封装出标准 ReAct 智能体：
 * <ul>
 *   <li>{@code ReactAgent.builder()} — 声明式装配：name / systemPrompt / model / tools / hooks</li>
 *   <li>{@code agent.call(input)} — 一次调用内部自动跑"思考 → 行动 → 观察"循环直至产出答案，
 *       返回 {@code AssistantMessage}</li>
 *   <li>Hook — 挂在模型调用前后的横切逻辑；这里用
 *       {@link ModelCallLimitHook} 限制单轮最多 5 次模型调用（防止失控循环），
 *       达到上限后 {@code exitBehavior(END)} 直接收尾而不是抛错</li>
 * </ul>
 *
 * <p>Agent 内部就是一张 Graph（{@code agent.getStateGraph()} 可取出），
 * 所以 Graph 模块里的 checkpoint / interrupt / 流式能力对 Agent 同样生效。
 */
@Service
public class ReactAgentService {

    private final ReactAgent agent;

    public ReactAgentService(ChatModel chatModel) throws GraphStateException {
        this.agent = ReactAgent.builder()
                .name("java-advisor")
                .description("Java 与 AI 领域的架构顾问")
                .systemPrompt("你是精通 Java 21、Spring Boot 4 和大模型应用开发的架构师。"
                        + "回答保持简洁专业，控制在 150 字以内。")
                .model(chatModel)
                .hooks(ModelCallLimitHook.builder()
                        .threadLimit(5)
                        .exitBehavior(ModelCallLimitHook.ExitBehavior.END)
                        .build())
                .build();
    }

    /** 调用 Agent：内部自动执行 ReAct 循环。 */
    public String ask(String message) throws GraphRunnerException {
        return agent.call(message).getText();
    }

    /** Agent 底层图的 Mermaid 定义：看清 ReAct 循环的图结构。 */
    public String mermaid() {
        var representation = agent.getStateGraph()
                .getGraph(com.alibaba.cloud.ai.graph.GraphRepresentation.Type.MERMAID, "react-agent");
        return representation.content();
    }
}
