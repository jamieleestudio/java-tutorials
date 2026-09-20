package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 智能客服接口。
 *
 * <p>GET /capstone/chat?threadId=…&amp;message=… — 客服入口（退款会停在人工确认）。
 * GET /capstone/confirm?threadId=…&amp;feedback=… — 人工确认退款后恢复执行。
 */
@RestController
public class CapstoneController {

    private final CapstoneService capstoneService;

    public CapstoneController(CapstoneService capstoneService) {
        this.capstoneService = capstoneService;
    }

    @GetMapping("/capstone/chat")
    public Map<String, Object> chat(
            @RequestParam(value = "threadId", required = false) String threadId,
            @RequestParam(value = "message",
                    defaultValue = "订单 1001 多久能到？另外，我想把订单 1002 退了")
            String message) throws Exception {
        return capstoneService.chat(threadId, message);
    }

    @PostMapping("/capstone/confirm")
    public Map<String, Object> confirm(
            @RequestParam("threadId") String threadId,
            @RequestParam(value = "feedback", defaultValue = "同意退款，7 天无理由政策适用，请跟进")
            String feedback) throws Exception {
        return capstoneService.confirm(threadId, feedback);
    }
}
