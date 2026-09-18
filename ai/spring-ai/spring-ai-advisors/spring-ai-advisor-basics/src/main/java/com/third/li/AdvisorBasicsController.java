package com.third.li;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * Advisor 基础（SimpleLoggerAdvisor + 自定义 BaseAdvisor）。
 *
 * <p>Advisor 是 Spring AI 的**中间件链**（类似 AgentScope 的 MiddlewareBase）：
 * <ul>
 *   <li>{@code before(request, chain)} — 请求发给模型<b>前</b>拦截/改写</li>
 *   <li>{@code after(response, chain)} — 模型返回<b>后</b>处理</li>
 * </ul>
 *
 * <p>本模块演示：
 * <ol>
 *   <li>内置 {@code SimpleLoggerAdvisor}：打印请求/响应日志</li>
 *   <li>自定义 {@code ToneAdvisor}：before 阶段注入语气系统提示</li>
 * </ol>
 */
@RestController
public class AdvisorBasicsController {

    private final ChatClient advised;
    private final ChatClient plain;

    public AdvisorBasicsController(ChatClient.Builder chatClientBuilder, ChatModel chatModel) {
        this.advised = chatClientBuilder
                .defaultAdvisors(new org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor())
                .defaultAdvisors(new ToneAdvisor("你是一个幽默风趣的助手，回答带点俏皮。"))
                .build();
        this.plain = ChatClient.create(chatModel);
    }

    /** 带 SimpleLoggerAdvisor + ToneAdvisor 的调用。 */
    @GetMapping("/ai/advisor")
    public String advisor(
            @RequestParam(value = "message", defaultValue = "介绍一下你自己") String message) {
        return advised.prompt(message).call().content();
    }

    /** 对照：不带 Advisor 的 ChatClient。 */
    @GetMapping("/ai/advisor/plain")
    public String plain(
            @RequestParam(value = "message", defaultValue = "介绍一下你自己") String message) {
        return plain.prompt(message).call().content();
    }

    /**
     * 自定义 Advisor：before 阶段往系统提示里注入语气。
     */
    public static class ToneAdvisor implements BaseAdvisor {

        private final String tone;

        public ToneAdvisor(String tone) {
            this.tone = tone;
        }

        @Override
        public int getOrder() {
            return 1;
        }

        @Override
        public String getName() {
            return "ToneAdvisor";
        }

        @Override
        public ChatClientRequest before(ChatClientRequest request, AdvisorChain chain) {
            // 在系统消息前插入语气提示
            List<org.springframework.ai.chat.messages.Message> msgs =
                    new ArrayList<>(request.prompt().getInstructions());
            msgs.add(0, new SystemMessage(tone));
            var newPrompt = new org.springframework.ai.chat.prompt.Prompt(msgs, request.prompt().getOptions());
            return request.mutate().prompt(newPrompt).build();
        }

        @Override
        public ChatClientResponse after(ChatClientResponse response, AdvisorChain chain) {
            return response; // 不改动响应
        }
    }
}
