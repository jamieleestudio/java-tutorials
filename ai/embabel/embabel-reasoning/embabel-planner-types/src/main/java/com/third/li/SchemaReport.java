package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

/** 一次类型构造的报告。 */
public record SchemaReport(
        @JsonPropertyDescription("类型名") String name,
        @JsonPropertyDescription("类型描述") String description,
        @JsonPropertyDescription("属性：name:cardinality:type") List<String> properties,
        @JsonPropertyDescription("框架渲染出的 schema 文本") String renderedSchema,
        @JsonPropertyDescription("字典里的领域类型数") int domainTypes,
        @JsonPropertyDescription("其中动态类型数") int dynamicTypes,
        @JsonPropertyDescription("其中 JVM 类型数") int jvmTypes,
        @JsonPropertyDescription("边界说明") String note) {
}
