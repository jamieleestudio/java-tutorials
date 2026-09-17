package com.third.li;

import com.embabel.agent.core.Cardinality;
import com.embabel.agent.core.DataDictionary;
import com.embabel.agent.core.DomainType;
import com.embabel.agent.core.DynamicType;
import com.embabel.agent.core.PropertyDefinition;
import com.embabel.agent.core.ValuePropertyDefinition;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * **运行时类型（DynamicType）**：不写 Java 类，也能定义"一个类型"。
 *
 * <p>关键认识：在 Embabel 里，**类型是规划器与 UX 共享的 schema**。
 * {@code Action}、{@code AgentProcess}、{@code AgentScope} 都实现 {@link DataDictionary}，
 * 规划器用它判断"黑板上现在有哪些类型、它们之间能有什么关系"；
 * UX 用它生成表单（见 {@code embabel-hitl-advanced} 的 {@code TypeRequest}）。
 *
 * <p>所以 {@link DynamicType} 的用途是：**运行时才确定的领域模型**
 * （用户自定义字段、从外部 schema 导入、多租户可配置字段……），
 * 让这些"事后才知道的类型"也能参与描述与规划。
 *
 * <h3>⚠️ 边界：DynamicType 不能直接用于结构化输出</h3>
 * {@code PromptRunner.createObject(prompt, Class<T>)} 只接受 {@code Class}，
 * 没有接受 {@code DynamicType}/{@code DataDictionary} 的重载。
 * 也就是说：**动态类型目前是"描述/规划层"能力，不是"数据绑定层"能力**。
 * 要把动态 schema 落到结构化输出，需要自己拼提示词并解析 JSON（或先用代码生成类）。
 * 本模块把这个边界明确写出来，避免你按"动态类型能直接 createObject"的预期去用。
 */
@RestController
public class TypeSchemaController {

    /** 从参数构造一个动态类型，并渲染它的 schema。 */
    @GetMapping("/types/build")
    public SchemaReport build(
            @RequestParam(value = "name", defaultValue = "ReviewNote") String name,
            @RequestParam(value = "fields", defaultValue = "title:string,tags:list,score:number,urgent:set")
                    String fields) {
        List<ValuePropertyDefinition> properties = Arrays.stream(fields.split(","))
                .map(String::trim)
                .filter(spec -> !spec.isEmpty())
                .map(this::toProperty)
                .toList();

        DynamicType type = new DynamicType(
                name,
                "运行时构造的类型（字段由请求参数决定）",
                properties,
                List.of(),
                true);

        DataDictionary dictionary = DataDictionary.fromDomainTypes("dynamic-" + name, List.of(type));

        return new SchemaReport(
                type.getName(),
                type.getDescription(),
                properties.stream()
                        .map(p -> "%s:%s:%s".formatted(p.getName(), p.getCardinality(), p.getType()))
                        .toList(),
                type.infoString(true, 0),
                dictionary.getDomainTypes().size(),
                dictionary.getDynamicTypes().size(),
                dictionary.getJvmTypes().size(),
                "DynamicType 是描述/规划层能力；createObject 只接受 Class，不直接用于结构化输出。");
    }

    /** 增量加字段：`withProperty` 返回新类型（不可变）。 */
    @GetMapping("/types/extend")
    public SchemaReport extend(
            @RequestParam(value = "name", defaultValue = "ReviewNote") String name,
            @RequestParam(value = "base", defaultValue = "title:string") String base,
            @RequestParam(value = "add", defaultValue = "reviewer:string") String add) {
        DynamicType type = new DynamicType(name, "增量扩展演示", List.of(), List.of(), true);
        type = type.withProperty(toProperty(base));
        DynamicType extended = type.withProperty(toProperty(add));

        DataDictionary dictionary = DataDictionary.fromDomainTypes("extended", List.of(extended));
        return new SchemaReport(
                extended.getName(),
                extended.getDescription(),
                extended.getOwnProperties().stream()
                        .map(ValuePropertyDefinition.class::cast)
                        .map(p -> "%s:%s:%s".formatted(p.getName(), p.getCardinality(), p.getType()))
                        .toList(),
                extended.infoString(true, 0),
                dictionary.getDomainTypes().size(),
                dictionary.getDynamicTypes().size(),
                dictionary.getJvmTypes().size(),
                "withProperty 是纯函数：每次返回新类型，所以可以安全地做「逐步累积字段」。");
    }

    /** 动态类型 + JVM 类型放进同一个字典，看规划器"眼中的类型宇宙"。 */
    @GetMapping("/types/dictionary")
    public SchemaReport dictionary() {
        DynamicType dynamic = new DynamicType(
                "ImportedRecord",
                "从外部 schema 导入的类型",
                List.of(new ValuePropertyDefinition("source", "string", Cardinality.ONE, "来源系统")),
                List.of(),
                true);

        DataDictionary dictionary = DataDictionary.fromDomainTypes("mixed", List.of(
                dynamic,
                new com.embabel.agent.core.JvmType(Article.class)));

        List<String> all = dictionary.getDomainTypes().stream()
                .map(DomainType::getName)
                .toList();

        return new SchemaReport(
                "mixed",
                "动态类型与 JVM 类型共存的字典",
                all.stream().map(n -> n + ":" + (dictionary.getDynamicTypes().stream()
                        .anyMatch(d -> d.getName().equals(n)) ? "dynamic" : "jvm")).toList(),
                "domainTypes=" + all + "\ndynamicTypes=" + dictionary.getDynamicTypes().size()
                        + "\njvmTypes=" + dictionary.getJvmTypes().size()
                        + "\nallowedRelationships=" + dictionary.allowedRelationships(),
                dictionary.getDomainTypes().size(),
                dictionary.getDynamicTypes().size(),
                dictionary.getJvmTypes().size(),
                "规划器与 UX 都通过 DataDictionary 认识类型；allowedRelationships 描述类型之间允许的关系。");
    }

    private ValuePropertyDefinition toProperty(String spec) {
        String[] parts = spec.split(":");
        String propertyName = parts[0].trim();
        String type = parts.length > 1 ? parts[1].trim().toLowerCase(Locale.ROOT) : "string";
        Cardinality cardinality = switch (type) {
            case "list" -> Cardinality.LIST;
            case "set" -> Cardinality.SET;
            case "optional" -> Cardinality.OPTIONAL;
            default -> Cardinality.ONE;
        };
        return new ValuePropertyDefinition(
                propertyName,
                type,
                cardinality,
                "字段 " + propertyName,
                Map.of());
    }
}
