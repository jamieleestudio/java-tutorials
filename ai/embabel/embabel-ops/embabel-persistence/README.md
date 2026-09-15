# embabel-persistence — 上下文持久化（Postgres）

## 演示内容

把 Embabel 的**上下文（Context）**——用户/会话级的长期状态——持久化到 Postgres，
应用**重启后仍能读回**，并且**保留对象的强类型**（不是退化成 Map）。
最后让 Agent 读取该上下文（用户画像）来回答问题。

## 关键 API

| API | 作用 |
|---|---|
| `ContextRepository` | 上下文仓库扩展点：`create` / `createWithId` / `save` / `findById` / `delete` |
| `Context` | 上下文：`bind(key, value)` / `addObject(obj)` / `objects` / `last(Class)` / `populate(blackboard)` |
| `InMemoryContext` | 框架提供的内存实现（本示例复用它作为"上下文对象"，只自己实现存取） |
| `@Primary` | 覆盖框架默认的 `contextRepository` Bean（它没有 `@ConditionalOnMissingBean`） |

## 接口

```bash
# 1) 保存上下文
curl -G --data-urlencode "userId=u1" --data-urlencode "name=张三" --data-urlencode "plan=pro" \
     http://localhost:8911/persistence/save

# 2) 重启应用后再读（验证持久化 + 类型还原）
curl -G --data-urlencode "userId=u1" http://localhost:8911/persistence/load
# -> {"found":true,"objects":[{"userId":"u1","name":"张三","plan":"pro"}],"types":["com.third.li.UserProfile"]}

# 3) Agent 使用持久化画像回答
curl -G --data-urlencode "userId=u1" --data-urlencode "message=推荐一个适合我的套餐" \
     http://localhost:8911/persistence/agent

# 4) 删除
curl -G --data-urlencode "userId=u1" http://localhost:8911/persistence/delete
```

## 运行

```bash
cd ai/embabel/docker && docker compose up -d postgres    # Postgres 在 5433
cd ai/embabel
mvn -pl :embabel-persistence spring-boot:run
```

连接信息可用 `POSTGRES_URL` / `POSTGRES_USER` / `POSTGRES_PASSWORD` 覆盖。

## 代码结构

- `PostgresContextRepository.java` — JDBC + Jackson 实现 `ContextRepository`（建表、upsert、类型化还原）
- `ContextAwareAgent.java` — 按用户 ID 读上下文，把画像写进提示词
- `PersistenceController.java` — save / load / agent / delete

## 要点（踩坑记录）

- **类型保真**：直接 `objectMapper.writeValueAsString(objects)` 会丢类型（读回来是 Map）。
  本示例每个对象存成 `{"type": "全限定类名", "value": {...}}`，读回时 `treeToValue(value, Class.forName(type))` 还原。
- **覆盖默认 Bean**：框架的 `agentProcessRepository` / `contextRepository` 没有
  `@ConditionalOnMissingBean`，所以要自定义实现必须加 `@Primary`。
- **为什么不做 AgentProcess 持久化**：`AgentProcess` 含黑板、规划器、历史等运行期对象，无法可靠序列化重建。
  生产上要恢复进程通常用**事件溯源**（写过程事件、重启重放），框架提供
  `AbstractAgentProcessRepository` 作为扩展点。
- 更完整的上下文用法是把 `contextId` 传给 `ProcessOptions`，平台会自动把上下文对象灌进黑板；
  本示例为保持 Java 侧简洁，在动作里显式读取。
