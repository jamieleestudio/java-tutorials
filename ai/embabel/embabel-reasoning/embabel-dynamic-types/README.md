# embabel-dynamic-types — 运行时领域类型

## 演示内容

不写 Java 类，也能定义"一个类型"——因为**在 Embabel 里，类型是规划器与 UX 共享的 schema**。

`Action`、`AgentProcess`、`AgentScope` **都实现 `DataDictionary`**，所以：
- **规划器**通过它判断"黑板上现在有哪些类型、类型之间能有什么关系"；
- **UX** 通过它生成表单（见 `embabel-hitl-advanced` 的 `TypeRequest`）。

`DynamicType` 的用途就是让**运行时才确定的领域模型**（用户自定义字段、外部 schema 导入、
多租户可配置字段）也能参与描述与规划。

## 实测

```bash
curl "http://localhost:8941/types/build?name=ReviewNote&fields=title:string,tags:list,score:number,urgent:set"
```

```json
{"name":"ReviewNote",
 "properties":["title:ONE:string","tags:LIST:list","score:ONE:number","urgent:SET:set"],
 "renderedSchema":"name: ReviewNote\nproperties:\n  ValuePropertyDefinition(name=title, type=string, cardinality=ONE, ...) ...",
 "domainTypes":1,"dynamicTypes":1,"jvmTypes":0}
```

```bash
curl "http://localhost:8941/types/dictionary"
```

```json
{"name":"mixed","properties":["ImportedRecord:dynamic","com.third.li.Article:jvm"],
 "renderedSchema":"domainTypes=[ImportedRecord, com.third.li.Article]\ndynamicTypes=1\njvmTypes=1\nallowedRelationships=[]",
 "domainTypes":2,"dynamicTypes":1,"jvmTypes":1}
```

## 关键 API

| API | 作用 |
|---|---|
| `new DynamicType(name, description, properties, parents, creationPermitted)` | 运行时构造类型 |
| `type.withProperty(pd)` | **纯函数**：返回新类型（可安全地逐步累积字段） |
| `new ValuePropertyDefinition(name, type, cardinality, description[, metadata])` | 字段定义（有 `@JvmOverloads`，Java 可少传参数） |
| `Cardinality.OPTIONAL / ONE / LIST / SET` | 字段基数 |
| `new JvmType(SomeClass.class)` | 把普通 Java 类包装成领域类型 |
| `DataDictionary.fromDomainTypes(name, types)` / `fromClasses(name, classes...)` | 组装"类型宇宙" |
| `dict.getDomainTypes()` / `getDynamicTypes()` / `getJvmTypes()` / `allowedRelationships()` | 查看字典内容 |
| `type.infoString(verbose, indent)` | 框架自带的 schema 渲染（LLM/UX 看到的就是这个） |
| `type.isAssignableFrom(...)` / `isAssignableTo(...)` / `children(...)` | 类型关系判断 |

## ⚠️ 重要边界：DynamicType **不能**直接用于结构化输出

`PromptRunner.createObject(prompt, Class<T>)` **只接受 `Class`**，
没有接受 `DynamicType` / `DataDictionary` 的重载。

也就是说：**动态类型目前是"描述 / 规划层"能力，不是"数据绑定层"能力。**
要把动态 schema 落到结构化输出，得自己拼提示词 + 解析 JSON（或用代码生成类再编译）。

把这条边界写出来，是为了避免你按"动态类型能直接 createObject"的预期去用——
这是读源码 + 实测后确认的结论，不是文档里的说法。

## 接口

```bash
curl "http://localhost:8941/types/build?fields=title:string,tags:list,score:number,urgent:set"
curl "http://localhost:8941/types/extend?base=title:string&add=reviewer:string"   # 增量加字段
curl "http://localhost:8941/types/dictionary"                                      # 动态类型 + JVM 类型共存
```

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-dynamic-types spring-boot:run
```

> 本模块**不需要 LLM / API Key**：纯类型构造与渲染。

## 代码结构

- `TypeSchemaController.java` — 从请求参数构造 `DynamicType`、渲染 schema、组装 `DataDictionary`
- `Article.java` — 一个普通 JVM 类型，用于和动态类型对照
- `SchemaReport.java` — 报告（属性、渲染后的 schema、字典统计、边界说明）

## 要点

- **`Cardinality` 决定"要不要生成表单控件"**：`ONE` 是单值，`LIST`/`SET` 是多值，
  `OPTIONAL` 可空。`TypeRequest` 的表单 schema 就是从这里来的。
- **`metadata` 承载语义**：`PropertyDefinition.metadata` 可以放 `predicate`/`inverse`/`aliases`
  这类语义信息（由 `@Semantics` + `@With(key, value)` 注解填充），
  用于让规划器理解"字段之间的语义关系"，而不仅是结构。
- **`allowedRelationships()`** 描述类型之间允许的关系（`AllowedRelationship`），
  是 `DataDictionary` 里比"字段"更高一层的信息；本模块实测为空（因为没声明关系）。
- **类型是契约也是规划依据**（贯穿整个仓库的主题）：所以"把领域概念建模清楚"
  在 Embabel 里不是风格问题，而是直接影响规划质量与 UX 生成质量。
