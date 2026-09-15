# embabel-hitl — 人机协同（Human-in-the-loop）

## 演示内容

让 Agent 在关键节点**暂停**，等人工确认或填写表单后再**恢复**。包含两种模式：

1. **确认型**：生成草稿 → 人工确认 → 产出最终结果
2. **表单型**：自动生成表单 → 用户提交 → 绑定成对象继续执行

## 关键 API

| API | 作用 |
|---|---|
| `WaitFor.confirmation(payload, desc)` | 暂停流程，等待确认；通过后 payload 提升到黑板 |
| `WaitFor.formSubmission(title, Class)` | 根据数据类生成表单并暂停 |
| `agentProcess.getStatus() == WAITING` | 判断流程是否在等待 |
| `process.addObject(payload); process.run()` | 确认通过后恢复流程 |
| `FormBindingRequest.bind(obj, process)` | 表单数据绑定后恢复流程 |

## 接口

```bash
# 确认型：启动 -> 返回 processId 与待确认内容
curl -G --data-urlencode "message=为一款智能咖啡机写一句广告语" http://localhost:8894/hitl/review
# 恢复（accepted=false 表示拒绝，不产出结果）
curl -X POST "http://localhost:8894/hitl/{processId}/confirm?accepted=true"

# 表单型：启动 -> 返回表单结构与 processId
curl -G --data-urlencode "message=客户想了解企业版报价" http://localhost:8894/hitl/contact
# 提交表单（Body 为 ContactInfo 的 JSON）
curl -X POST -H "Content-Type: application/json" \
     -d '{"name":"Zhao Liu","email":"zhao@example.com"}' \
     "http://localhost:8894/hitl/{processId}/form"
```

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-hitl spring-boot:run
```

## 代码结构

- `ReviewAgent.java` — `WaitFor.confirmation(new ChatReply(draft), "...")`
- `ContactFormAgent.java` — `collect(UserInput) -> ContactInfo` + `reply(ContactInfo, UserInput, Ai) -> ContactReply`
- `HitlController.java` — 启动 / 确认 / 提交表单 / 状态查询

## 要点（务必注意）

- **等待对象的 payload 类型必须等于该 Action 的返回类型**。否则确认通过后目标无法满足，
  恢复后的状态会变成 `STUCK`。
- **表单型要拆成两个动作**：第一个动作返回绑定类型（`ContactInfo`），第二个动作消费它产出最终结果。
- 拒绝确认时不要调用 `run()`，流程会保持暂停（本示例直接返回 `REJECTED`）。
- 生产环境通常配合 `GET /api/v1/process/{id}` 或 SSE 事件流把等待状态推给前端。
