package com.third.li;

import com.embabel.agent.api.validation.guardrails.UserInputGuardRail;
import com.embabel.agent.core.Blackboard;
import com.embabel.common.core.validation.ValidationError;
import com.embabel.common.core.validation.ValidationResult;
import com.embabel.common.core.validation.ValidationSeverity;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 输入护栏：拦截疑似**个人敏感信息（PII）**的输入。
 *
 * <p>示例检测：身份证号、手机号、邮箱。命中返回 CRITICAL（阻断本次调用），
 * 提示用户不要通过对话提交敏感信息（生产上应改为走加密表单/专用通道）。
 */
public class PiiInputGuardRail implements UserInputGuardRail {

    static final Pattern ID_CARD = Pattern.compile("\\d{17}[\\dXx]");
    static final Pattern PHONE = Pattern.compile("1[3-9]\\d{9}");
    static final Pattern EMAIL = Pattern.compile("[\\w.+-]+@[\\w-]+\\.[\\w.]+");

    @Override
    public String getName() {
        return "PiiInputGuardRail";
    }

    @Override
    public String getDescription() {
        return "拦截包含身份证/手机号/邮箱等个人敏感信息的输入";
    }

    @Override
    public ValidationResult validate(String input, Blackboard blackboard) {
        for (String hit : PiiDetector.detect(input)) {
            return new ValidationResult(false, List.of(new ValidationError(
                    "PII_DETECTED",
                    "输入包含疑似个人敏感信息（" + hit + "），请勿通过对话提交，改用专用加密通道",
                    ValidationSeverity.CRITICAL)));
        }
        return new ValidationResult(true, List.of());
    }
}
