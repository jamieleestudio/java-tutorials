package com.third.li;

import com.embabel.agent.api.validation.guardrails.UserInputGuardRail;
import com.embabel.agent.core.Blackboard;
import com.embabel.common.core.validation.ValidationError;
import com.embabel.common.core.validation.ValidationResult;
import com.embabel.common.core.validation.ValidationSeverity;

import java.util.List;
import java.util.Locale;

/**
 * 输入护栏：拦截提示词注入类输入。
 *
 * <p>实现 {@link UserInputGuardRail}，在调用 LLM 之前校验用户输入。
 * 返回 CRITICAL 级别的错误会抛出 {@code GuardRailViolationException} 终止本次调用；
 * WARNING / ERROR 只记日志，不阻断。
 */
public class InjectionGuardRail implements UserInputGuardRail {

    private static final List<String> SUSPICIOUS = List.of(
            "忽略之前", "忽略上面", "ignore previous", "ignore all previous",
            "system prompt", "系统提示词", "泄露提示词", "你现在是");

    @Override
    public String getName() {
        return "InjectionGuardRail";
    }

    @Override
    public String getDescription() {
        return "拦截疑似提示词注入的用户输入";
    }

    @Override
    public ValidationResult validate(String input, Blackboard blackboard) {
        String normalized = input.toLowerCase(Locale.ROOT);
        return SUSPICIOUS.stream()
                .filter(pattern -> normalized.contains(pattern.toLowerCase(Locale.ROOT)))
                .findFirst()
                .map(hit -> new ValidationResult(false, List.of(new ValidationError(
                        "PROMPT_INJECTION",
                        "检测到疑似提示词注入（命中关键词：" + hit + "）",
                        ValidationSeverity.CRITICAL))))
                .orElseGet(() -> new ValidationResult(true, List.of()));
    }
}
