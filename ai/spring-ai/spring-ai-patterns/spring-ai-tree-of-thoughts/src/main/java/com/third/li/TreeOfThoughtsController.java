package com.third.li;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Tree of Thoughts 模式（思维树）。
 *
 * <p>多个 ChatClient（不同 temperature）并行生成候选思路 → 评分 → 最优深化。
 * 与 Embabel embabel-tree-of-thoughts / AgentScope agentscope-tree-of-thoughts 对照。
 */
@RestController
public class TreeOfThoughtsController {

    private final ChatClient chatClient;

    public TreeOfThoughtsController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @GetMapping("/ai/tot")
    public String solve(
            @RequestParam(value = "message", defaultValue = "如何降低企业软件开发成本") String message) {
        // 1. 三个不同 temperature 的分支并行生成思路
        List<String> thoughts = Flux.merge(
                think(message, 0.2, "严谨务实"),
                think(message, 0.7, "创新发散"),
                think(message, 1.2, "大胆激进")
        ).collectList().block();

        // 2. 评分选择最优
        String scored = chatClient.prompt()
                .system("你是评估专家。给每个思路打分（1-10）并推荐得分最高的。")
                .user("思路1：\n" + thoughts.get(0) + "\n\n思路2：\n" + thoughts.get(1)
                        + "\n\n思路3：\n" + thoughts.get(2) + "\n\n输出格式：思路X：Y分。推荐：思路X")
                .call().content();

        // 3. 深化最优思路
        String detail = chatClient.prompt()
                .system("你是方案专家，把思路展开成详细可执行的方案。")
                .user("基于评估结果，深化最优思路。\n\n评估：\n" + scored
                        + "\n\n各思路：\n思路1：" + thoughts.get(0)
                        + "\n思路2：" + thoughts.get(1)
                        + "\n思路3：" + thoughts.get(2))
                .call().content();

        return "【思维树】\n"
                + "\n===== 分支 1（temp=0.2）=====\n" + thoughts.get(0)
                + "\n\n===== 分支 2（temp=0.7）=====\n" + thoughts.get(1)
                + "\n\n===== 分支 3（temp=1.2）=====\n" + thoughts.get(2)
                + "\n\n===== 评分 =====\n" + scored
                + "\n\n===== 最优深化 =====\n" + detail;
    }

    private Mono<String> think(String problem, double temp, String style) {
        return Mono.fromCallable(() -> chatClient.prompt()
                .user(problem + "\n请用" + style + "的思路给出一个解决方案。")
                .options(ChatOptions.builder().temperature(temp))
                .call().content())
                .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic());
    }
}
