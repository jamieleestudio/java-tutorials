package com.third.li;

import java.util.ArrayList;
import java.util.List;

/** PII 检测的公共逻辑（输入/输出护栏共用）。 */
final class PiiDetector {

    private PiiDetector() {
    }

    static List<String> detect(String text) {
        List<String> hits = new ArrayList<>();
        if (text == null) {
            return hits;
        }
        if (PiiInputGuardRail.ID_CARD.matcher(text).find()) {
            hits.add("身份证号");
        }
        if (PiiInputGuardRail.PHONE.matcher(text).find()) {
            hits.add("手机号");
        }
        if (PiiInputGuardRail.EMAIL.matcher(text).find()) {
            hits.add("邮箱");
        }
        return hits;
    }
}
