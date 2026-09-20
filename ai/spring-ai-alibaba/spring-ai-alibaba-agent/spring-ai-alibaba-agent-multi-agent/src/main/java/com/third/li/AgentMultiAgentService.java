package com.third.li;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.extension.interceptor.SubAgentInterceptor;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

/**
 * 多 Agent 协作：主管 Agent + SubAgentInterceptor 委派子 Agent。
 *
 * <p>模式："主管-专家"。主管不亲自干活，而是通过子 Agent 工具把任务分派出去：
 * <ul>
 *   <li>先各自构建专家 {@link ReactAgent}（translator / summarizer），各带自己的 systemPrompt</li>
 *   <li>主管 builder 挂上 {@link SubAgentInterceptor}：
 *       {@code .addSubAgent("translator", translator)} —— 主管的上下文里会多出
 *       委派子 Agent 的工具，模型调用它即把任务转交给对应专家并取回结果</li>
 * </ul>
 *
 * <p>对照 {@code agentscope-subagent}（SubagentDeclaration）与
 * {@code embabel-supervisor}（GOAP 主管）：同样是"主管决策、专家执行"，只是委派机制不同。
 */
@Service
public class AgentMultiAgentService {

    private final ReactAgent coordinator;

    public AgentMultiAgentService(ChatModel chatModel) throws GraphStateException {
        ReactAgent translator = ReactAgent.builder()
                .name("translator")
                .description("中英翻译专家：把文本在中译英或英译中")
                .systemPrompt("你是专业译者。直接输出译文，不要解释。")
                .model(chatModel)
                .build();

        ReactAgent summarizer = ReactAgent.builder()
                .name("summarizer")
                .description("摘要专家：把长文本压缩成一句话摘要")
                .systemPrompt("你是摘要专家。输出一句话中文摘要，不超过 40 字。")
                .model(chatModel)
                .build();

        this.coordinator = ReactAgent.builder()
                .name("coordinator")
                .description("任务协调主管：判断该找翻译还是摘要专家")
                .systemPrompt("你是任务主管。根据用户请求委派合适的专家（translator/summarizer）完成任务，"
                        + "拿到专家结果后原样输出给用户，不要自己翻译或摘要。")
                .model(chatModel)
                .interceptors(SubAgentInterceptor.builder()
                        .addSubAgent("translator", translator)
                        .addSubAgent("summarizer", summarizer)
                        .build())
                .build();
    }

    public String ask(String message) throws GraphRunnerException {
        return coordinator.call(message).getText();
    }
}
