package com.third.li;

import org.springframework.stereotype.Component;

/**
 * 一个普通的 Spring 组件，用于演示 {@code @Provided} 注入。
 */
@Component
public class ReplyFormatter {

    public String wrap(String content) {
        return "【格式：Markdown】\n" + content;
    }

    public String formatName() {
        return "Markdown";
    }
}
