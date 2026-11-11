package com.third.li;

import dev.langchain4j.model.chat.router.ChatModelRoutingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** 决策模型接口：GET /decisions/… */
@RestController
public class DecisionModelsController {

    private final DecisionModelsConfig.TicketDecisions ticketDecisions;
    private final DecisionModelsConfig.ChatModelChatModelRoutingSupport routingSupport;
    private final DecisionModelsConfig config;

    public DecisionModelsController(DecisionModelsConfig.TicketDecisions ticketDecisions,
                                    DecisionModelsConfig.ChatModelChatModelRoutingSupport routingSupport,
                                    DecisionModelsConfig config) {
        this.ticketDecisions = ticketDecisions;
        this.routingSupport = routingSupport;
        this.config = config;
    }

    /** @Decide 布尔决策：一次调用返回 yes/no 概率。 */
    @GetMapping("/decisions/complaint")
    public Map<String, Object> complaint(
            @RequestParam(defaultValue = "用了三天就坏了，你们这是什么质量！必须给我退货赔偿！") String message) {
        boolean complaint = ticketDecisions.isComplaint(message);
        return Map.of("message", message, "isComplaint", complaint);
    }

    /** @Decide 枚举决策：返回枚举 + 概率分布。 */
    @GetMapping("/decisions/team")
    public Map<String, Object> team(
            @RequestParam(defaultValue = "我的账单好像多扣了一笔钱，帮我查一下") String message) {
        DecisionModelsConfig.Team team = ticketDecisions.team(message);
        return Map.of("message", message, "team", team);
    }

    /** 决策模型路由：按路由名选模型（simple=便宜，complex=贵）。 */
    @GetMapping("/decisions/route")
    public Map<String, Object> route(
            @RequestParam(defaultValue = "帮我分析这段代码的并发安全问题，并给出修复方案") String message) {
        ChatModelRoutingResult result = config.route(routingSupport, message);
        String model = result.isDefaultRoute()
                ? "simple(默认)"
                : result.routeName();
        return Map.of("message", message, "route", result.routeName(), "wouldUse", model);
    }
}
