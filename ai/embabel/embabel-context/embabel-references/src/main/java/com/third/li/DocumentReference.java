package com.third.li;

import com.embabel.agent.api.reference.LlmReference;

/**
 * 自定义 {@link LlmReference}：把一段文本作为参考资料注入提示词。
 *
 * <p>为什么不直接用框架内置的 {@code SpringResource} / {@code LiteralText}？
 * 它们的默认 {@code contribution()} 里会打印 {@code Tool prefix: ...} 一行，
 * 而它们本身并不提供工具。DeepSeek 看到 "Tool prefix" 后会"脑补"出一个
 * {@code <tool_calls>} 调用来读取资料（工具列表实际为空，调用无法执行），
 * 导致回答不可用。
 *
 * <p>因此这里重写 {@code contribution()}，只输出名称、说明与内容，避免歧义。
 * 内置实现适合"确实提供工具"的引用（如 API 文档 + 调用工具）；
 * 纯文本资料建议像本示例这样自定义。
 */
public class DocumentReference implements LlmReference {

    private final String name;
    private final String description;
    private final String content;

    public DocumentReference(String name, String description, String content) {
        this.name = name;
        this.description = description;
        this.content = content;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String notes() {
        return content;
    }

    @Override
    public String contribution() {
        return """
                【参考资料：%s】
                %s
                ---
                %s
                """.formatted(name, description, content);
    }
}
