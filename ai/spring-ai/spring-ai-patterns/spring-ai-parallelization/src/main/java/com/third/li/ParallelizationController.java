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
 * Parallelization 模式（并行 + 投票）。
 *
 * <p>用 Reactor {@link Flux#merge} 让多个 ChatClient 并发执行，合并结果。
 * 与 Embabel Kotlin ScatterGather / AgentScope Flux.merge 对照。
 *
 * <p>两种子模式：
 * <ul>
 *   <li><b>Sectioning</b>：按维度拆给多 Agent 并行分析，再汇总</li>
 *   <li><b>Voting</b>：多 Agent 独立回答，多数一致作为最终结果</li>
 * </ul>
 */
@RestController
public class ParallelizationController {

    private final ChatClient financeClient;
    private final ChatClient techClient;
    private final ChatClient riskClient;

    public ParallelizationController(ChatClient.Builder chatClientBuilder) {
        this.financeClient = chatClientBuilder
                .defaultSystem("你是财务分析师，从成本收益角度分析。")
                .build();
        this.techClient = chatClientBuilder
                .defaultSystem("你是技术专家，从可行性角度分析。")
                .build();
        this.riskClient = chatClientBuilder
                .defaultSystem("你是风险专家，从风险角度分析，指出隐患。")
                .build();
    }

    /** Sectioning：三个 ChatClient 并行分析不同维度。 */
    @GetMapping("/ai/parallel")
    public String sectioning(
            @RequestParam(value = "message", defaultValue = "评估建设一个新的电商平台") String message) {
        // 每个维度用 Mono.defer + fromCallable 包成异步任务，并发执行
        List<Mono<String>> tasks = List.of(
                Mono.fromCallable(() -> financeClient.prompt(message).call().content()).subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic()),
                Mono.fromCallable(() -> techClient.prompt(message).call().content()).subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic()),
                Mono.fromCallable(() -> riskClient.prompt(message).call().content()).subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic()));

        List<String> results = Flux.merge(tasks).collectList().block();
        return "【Sectioning 并行分析】\n"
                + "\n--- 财务 ---\n" + results.get(0)
                + "\n\n--- 技术 ---\n" + results.get(1)
                + "\n\n--- 风险 ---\n" + results.get(2);
    }
}
