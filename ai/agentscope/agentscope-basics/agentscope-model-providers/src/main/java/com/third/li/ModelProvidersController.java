package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 多模型接口。
 *
 * <p>三个端点：
 * <ul>
 *   <li>{@code /model/primary} — 用主模型（Flash，快且便宜）</li>
 *   <li>{@code /model/pro} — 用 Pro 模型（更强但更贵）</li>
 *   <li>{@code /model/info} — 当前配置</li>
 * </ul>
 */
@RestController
public class ModelProvidersController {

    private final MultiModelAgent agent;

    public ModelProvidersController(MultiModelAgent agent) {
        this.agent = agent;
    }

    @GetMapping("/model/primary")
    public String primary(
            @RequestParam(value = "message", defaultValue = "用一句话解释什么是 MoE 架构") String message) {
        return agent.chatWithPrimary(message);
    }

    @GetMapping("/model/pro")
    public String pro(
            @RequestParam(value = "message", defaultValue = "用一句话解释什么是 MoE 架构") String message) {
        return agent.chatWithModel(message, "pro");
    }

    @GetMapping("/model/info")
    public Map<String, String> info() {
        Map<String, String> info = new LinkedHashMap<>();
        info.put("primary", "deepseek-v4-flash @ https://api.deepseek.com");
        info.put("fallback", "deepseek-v4-pro @ https://api.deepseek.com");
        info.put("note", "主模型失败时自动切 fallback；/model/pro 手动切到 Pro");
        return info;
    }
}