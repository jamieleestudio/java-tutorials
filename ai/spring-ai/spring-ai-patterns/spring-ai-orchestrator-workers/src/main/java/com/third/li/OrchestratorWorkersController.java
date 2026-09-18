package com.third.li;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Orchestrator-Workers 模式（编排者-工人）。
 *
 * <p>主 Agent（编排者）拆解任务、分配工人、收集结果。工人用 {@link Tool}
 * 注解包装成函数，主 Agent 的模型可以自主调用。
 *
 * <p>与 Embabel @Action 拆解 / AgentScope SubagentDeclaration 对照——
 * Spring AI 用"工具即工人"实现委派。
 */
@RestController
public class OrchestratorWorkersController {

    private final ChatClient orchestrator;
    private final ChatModel chatModel;

    public OrchestratorWorkersController(ChatClient.Builder chatClientBuilder, ChatModel chatModel) {
        this.chatModel = chatModel;
        this.orchestrator = chatClientBuilder
                .defaultSystem("""
                        你是编排者。职责：
                        1. 分析用户任务
                        2. 需要写代码时调用 writeCode 工人
                        3. 需要审查代码时调用 reviewCode 工人
                        4. 汇总工人结果交付

                        其他问题直接回答。
                        """)
                .defaultTools(new CodeWorkers())
                .build();
    }

    /** 主 Agent 编排：模型自主决定调用哪个工人。 */
    @GetMapping("/ai/orchestrator")
    public String orchestrate(
            @RequestParam(value = "message", defaultValue = "写一个计算两个数之和的 Java 方法并审查") String message) {
        return orchestrator.prompt(message).call().content();
    }

    /** 工人工具集：用 ChatClient 作为工人实现。 */
    public class CodeWorkers {

        @Tool(name = "writeCode", description = "代码工人：编写代码实现")
        public String writeCode(@ToolParam(description = "编码需求") String requirement) {
            var worker = ChatClient.create(chatModel);
            return worker.prompt()
                    .system("你是编码工人，输出完整可运行的代码。")
                    .user(requirement)
                    .call().content();
        }

        @Tool(name = "reviewCode", description = "审查工人：审查代码质量")
        public String reviewCode(@ToolParam(description = "待审查的代码") String code) {
            var worker = ChatClient.create(chatModel);
            return worker.prompt()
                    .system("你是代码审查员，找出问题并给出改进建议。")
                    .user(code)
                    .call().content();
        }
    }
}
