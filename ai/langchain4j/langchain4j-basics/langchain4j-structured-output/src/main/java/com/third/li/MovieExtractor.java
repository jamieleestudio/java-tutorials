package com.third.li;

import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 结构化输出 AI 服务：方法返回类型即输出 schema。
 *
 * <p>LangChain4j 会根据返回类型自动生成 JSON Schema 约束并把模型输出
 * 反序列化为强类型对象 —— 与 Spring AI 的 entity(Class)、embabel 的
 * 强类型 return 同一能力。
 */
public interface MovieExtractor {

    record MovieInfo(String title, Integer year, String director, String genre, Double rating) {
    }

    @UserMessage("""
            从下面的介绍中提取电影信息。
            介绍：{{text}}
            """)
    MovieInfo extract(@V("text") String text);
}
