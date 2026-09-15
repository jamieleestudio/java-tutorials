package com.third.li;

import com.embabel.agent.api.validation.guardrails.AssistantMessageGuardRail;
import com.embabel.agent.core.Blackboard;
import com.embabel.common.core.thinking.ThinkingResponse;
import com.embabel.common.core.validation.ValidationError;
import com.embabel.common.core.validation.ValidationResult;
import com.embabel.common.core.validation.ValidationSeverity;

import java.util.List;

/**
 * 输出护栏：检查模型回复里是否包含敏感信息。
 *
 * <p>实现 {@link AssistantMessageGuardRail}，在模型返回之后校验内容：
 * 命中密钥/机密字样返回 CRITICAL 阻断；内容过长返回 WARNING（仅记录）。
 */
public class SensitiveOutputGuardRail implements AssistantMessageGuardRail {

    private static final List<String> SENSITIVE = List.of("sk-", "机密", "内部密码", "password=");

    @Override
    public String getName() {
        return "SensitiveOutputGuardRail";
    }

    @Override
    public String getDescription() {
        return "检查模型输出是否包含密钥或机密信息";
    }

    @Override
    public ValidationResult validate(String input, Blackboard blackboard) {
        String normalized = input.toLowerCase();
        return SENSITIVE.stream()
                .filter(normalized::contains)
                .findFirst()
                .map(hit -> new ValidationResult(false, List.of(new ValidationError(
                        "SENSITIVE_CONTENT",
                        "模型输出疑似包含敏感信息（命中：" + hit + "）",
                        ValidationSeverity.CRITICAL))))
                .orElseGet(() -> input.length() > 800
                        ? new ValidationResult(false, List.of(new ValidationError(
                        "RESPONSE_TOO_LONG",
                        "回复长度 " + input.length() + " 超过建议上限",
                        ValidationSeverity.WARNING)))
                        : new ValidationResult(true, List.of()));
    }

    @Override
    public ValidationResult validate(ThinkingResponse<?> response, Blackboard blackboard) {
        return validate(String.valueOf(response.getResult()), blackboard);
    }
}
