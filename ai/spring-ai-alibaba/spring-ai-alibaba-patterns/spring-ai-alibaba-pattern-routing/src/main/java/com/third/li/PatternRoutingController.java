package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 路由模式接口：GET /patterns/routing/ask
 */
@RestController
public class PatternRoutingController {

    private final PatternRoutingService patternService;

    public PatternRoutingController(PatternRoutingService patternService) {
        this.patternService = patternService;
    }

    @GetMapping("/patterns/routing/ask")
    public Map<String, Object> ask(
            @RequestParam(value = "message", defaultValue = "我上个月被重复扣了一次会员费，请处理一下")
            String message) throws Exception {
        return patternService.route(message);
    }
}
