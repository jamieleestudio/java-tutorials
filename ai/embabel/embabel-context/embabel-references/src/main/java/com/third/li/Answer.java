package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

/**
 * 回答及本次注入的参考资料。
 */
public record Answer(
        @JsonPropertyDescription("回答正文") String answer,
        @JsonPropertyDescription("本次使用的参考资料名称") List<String> references) {
}
