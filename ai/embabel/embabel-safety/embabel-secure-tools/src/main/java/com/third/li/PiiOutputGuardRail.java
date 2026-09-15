package com.third.li;

import com.embabel.agent.api.validation.guardrails.AssistantMessageGuardRail;
import com.embabel.agent.core.Blackboard;
import com.embabel.common.core.thinking.ThinkingResponse;
import com.embabel.common.core.validation.ValidationError;
import com.embabel.common.core.validation.ValidationResult;
import com.embabel.common.core.validation.ValidationSeverity;

import java.util.List;

/**
 * 输出护栏：检查模型回复是否**泄漏**个人敏感信息。
 *
 * <p>护栏只能"校验/阻断"，不能改写内容；命中即阻断，让调用方知道需要走脱敏流程。
 */
public class PiiOutputGuardRail implements AssistantMessageGuardRail {

    @Override
    public String getName() {
        return "PiiOutputGuardRail";
    }

    @Override
    public String getDescription() {
        return "检查模型输出是否泄漏身份证/手机号/邮箱";
    }

    @Override
    public ValidationResult validate(String input, Blackboard blackboard) {
        for (String hit : PiiDetector.detect(input)) {
            return new ValidationResult(false, List.of(new ValidationError(
                    "PII_LEAK",
                    "模型输出包含疑似个人敏感信息（" + hit + "），已阻断",
                    ValidationSeverity.CRITICAL)));
        }
        return new ValidationResult(true, List.of());
    }

    @Override
    public ValidationResult validate(ThinkingResponse<?> response, Blackboard blackboard) {
        return validate(String.valueOf(response.getResult()), blackboard);
    }
}
