package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 图像理解结果。
 */
public record Description(
        @JsonPropertyDescription("图片路径") String imagePath,
        @JsonPropertyDescription("模型对图片的描述") String description) {
}
