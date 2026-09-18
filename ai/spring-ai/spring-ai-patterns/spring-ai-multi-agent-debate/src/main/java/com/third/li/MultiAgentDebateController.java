package com.third.li;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Multi-Agent Debate 模式（多 Agent 辩论）。
 *
 * <p>正方/反方两个 ChatClient（对立 system prompt）并发发言，裁判 ChatClient
 * 综合双方观点给出裁定。与 Embabel embabel-debate / AgentScope agentscope-debate 对照。
 */
@RestController
public class MultiAgentDebateController {

    private final ChatClient pro;
    private final ChatClient con;
    private final ChatClient judge;

    public MultiAgentDebateController(ChatClient.Builder builder, ChatModel chatModel) {
        this.pro = builder.defaultSystem("你是正方辩手，坚定支持议题，给出有力论点。").build();
        this.con = builder.defaultSystem("你是反方辩手，坚定反对议题，给出有力反驳。").build();
        this.judge = ChatClient.create(chatModel);
    }

    @GetMapping("/ai/debate")
    public String debate(
            @RequestParam(value = "message", defaultValue = "远程办公应该成为主流工作方式") String message) {
        // 正反方并发发言（包成异步任务）
        List<String> sides = Flux.merge(
                Mono.fromCallable(() -> pro.prompt().user("议题：" + message + "\n给出 3 条支持论点").call().content())
                        .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic()),
                Mono.fromCallable(() -> con.prompt().user("议题：" + message + "\n给出 3 条反对论点").call().content())
                        .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic())
        ).collectList().block();

        String proText = sides.get(0);
        String conText = sides.get(1);

        String verdict = judge.prompt()
                .system("你是中立裁判，综合双方论点给出公平裁定。")
                .user("议题：" + message + "\n\n正方：\n" + proText + "\n\n反方：\n" + conText
                        + "\n\n请给出最终裁定和理由。")
                .call().content();

        return "【辩论】" + message
                + "\n\n===== 正方 =====\n" + proText
                + "\n\n===== 反方 =====\n" + conText
                + "\n\n===== 裁判 =====\n" + verdict;
    }
}
