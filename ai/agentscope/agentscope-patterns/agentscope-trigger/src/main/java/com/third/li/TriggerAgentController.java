package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 反应式触发接口。 */
@RestController
public class TriggerAgentController {

    private final TriggerAgent agent;

    public TriggerAgentController(TriggerAgent agent) {
        this.agent = agent;
    }

    /** 触发 Hook：含"紧急"关键词时注入紧急上下文。 */
    @GetMapping("/patterns/trigger/ask")
    public String ask(
            @RequestParam(value = "message", defaultValue = "紧急：我的订单还没发货，请处理") String message) {
        return agent.chat(message);
    }

    /** 查看触发统计。 */
    @GetMapping("/patterns/trigger/stats")
    public String stats() {
        return agent.stats();
    }
}