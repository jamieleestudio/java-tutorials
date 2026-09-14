package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

/**
 * 强类型输出模型。
 *
 * <p>{@link JsonPropertyDescription} 的说明会写进提示词中，
 * 帮助模型理解每个字段该填什么。
 */
public record Profile(
        @JsonPropertyDescription("姓名") String name,
        @JsonPropertyDescription("年龄") int age,
        @JsonPropertyDescription("掌握的技能列表") List<String> skills,
        @JsonPropertyDescription("一句话总结") String summary) {
}
