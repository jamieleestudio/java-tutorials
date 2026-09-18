package com.third.li;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * State Machine 模式（状态机 / 阶段收敛）。
 *
 * <p>用自定义 Advisor 在请求前根据<b>当前状态</b>注入不同的系统提示：
 * <ul>
 *   <li><b>PLAN 状态</b>：只读分析，禁止修改操作</li>
 *   <li><b>ACT 状态</b>：允许执行操作</li>
 * </ul>
 *
 * <p>与 AgentScope 的 PlanMode（enterPlanMode/exitPlanMode）、Embabel 的
 * state-machine 对照——Spring AI 用 Advisor 动态切换阶段。
 */
@RestController
public class StateMachineController {

    private final AtomicReference<String> state = new AtomicReference<>("PLAN");
    private final ChatClient chatClient;

    public StateMachineController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(new StateAdvisor(state))
                .build();
    }

    /** 切换状态。 */
    @GetMapping("/ai/state-machine/state")
    public String setState(@RequestParam(value = "state", defaultValue = "ACT") String newState) {
        state.set("PLAN".equalsIgnoreCase(newState) ? "PLAN" : "ACT");
        return "状态已切换：" + state.get();
    }

    /** 查看状态。 */
    @GetMapping("/ai/state-machine/status")
    public String status() {
        return "当前状态：" + state.get();
    }

    /** 在状态约束下对话。 */
    @GetMapping("/ai/state-machine")
    public String chat(
            @RequestParam(value = "message", defaultValue = "分析项目技术选型") String message) {
        return chatClient.prompt(message).call().content();
    }

    /** 状态感知 Advisor：按状态注入不同系统提示。 */
    public static class StateAdvisor implements BaseAdvisor {

        private final AtomicReference<String> state;

        public StateAdvisor(AtomicReference<String> state) {
            this.state = state;
        }

        @Override
        public int getOrder() {
            return 1;
        }

        @Override
        public String getName() {
            return "StateAdvisor";
        }

        @Override
        public ChatClientRequest before(ChatClientRequest request, AdvisorChain chain) {
            String prompt = "PLAN".equals(state.get())
                    ? "当前处于 PLAN（计划）状态：只做分析和规划，不要执行任何修改操作。"
                    : "当前处于 ACT（执行）状态：可以执行操作和修改。";

            List<org.springframework.ai.chat.messages.Message> msgs =
                    new ArrayList<>(request.prompt().getInstructions());
            msgs.add(0, new SystemMessage(prompt));
            var newPrompt = new org.springframework.ai.chat.prompt.Prompt(msgs, request.prompt().getOptions());
            return request.mutate().prompt(newPrompt).build();
        }

        @Override
        public ChatClientResponse after(ChatClientResponse response, AdvisorChain chain) {
            return response;
        }
    }
}
