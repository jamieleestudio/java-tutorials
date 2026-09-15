# embabel-secure-tools — 工具安全（最小权限 + PII 护栏）

## 演示内容

Agent 安全的三层防护：

1. **最小权限**：只暴露**只读且已脱敏**的工具；写操作（导出/修改/取消）根本不暴露给模型
2. **PII 护栏**：输入/输出双向检测身份证、手机号、邮箱，命中 CRITICAL 直接阻断
3. **敏感操作确认**：真正需要写操作时走人工审批（`embabel-hitl`）或应用层显式确认

## 关键 API

| API | 作用 |
|---|---|
| 只暴露必要工具（`withTools(Tool.fromInstance(new ReadOnlyOrderTools()))`） | 最有效的安全边界 |
| `UserInputGuardRail` / `AssistantMessageGuardRail` | 输入/输出护栏（本模块做 PII 检测） |
| `ValidationSeverity.CRITICAL` | 命中即阻断本次调用 |
| `embabel.agent.platform.action-qos` / `data-binding.max-attempts=1` | 护栏触发时快速失败 |

## 接口

```bash
# 正常：只读工具 + 已脱敏结果
curl -G --data-urlencode "message=A1001 订单到哪了" http://localhost:8924/secure/ask
# -> {"blocked":false,"answer":"订单 A1001 当前状态：已发货（收件人 张*）..."}

# PII 输入：被护栏拦截
curl -G --data-urlencode "message=我的手机号是 13800001111，帮我查一下订单" http://localhost:8924/secure/ask
# -> {"blocked":true,"reason":"GuardRail 'PiiInputGuardRail' violation: 输入包含疑似个人敏感信息（手机号）..."}

# 写操作：模型明确拒绝（因为没暴露写工具）
curl -G --data-urlencode "message=帮我导出所有客户数据" http://localhost:8924/secure/ask
```

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-secure-tools spring-boot:run
```

## 代码结构

- `ReadOnlyOrderTools.java` — 只读、已脱敏的工具集（**故意不含写操作**）
- `PiiInputGuardRail.java` / `PiiOutputGuardRail.java` / `PiiDetector.java` — PII 检测
- `SecureAgent.java` — 挂护栏 + 最小权限工具 + 明确的行为约束
- `SecureController.java` — `GET /secure/ask`（返回 blocked 状态）

## 要点

- **"不暴露"比"事后过滤"可靠**：模型无法调用不存在的工具，这是最硬的边界。
- 护栏只能**校验/阻断**、不能改写内容；需要脱敏应在工具层或返回前处理。
- PII 检测用正则只是示例；生产上应结合实体识别（NER）与业务字段白名单。
- 与 `embabel-guardrails` 的分工：那里演示"注入攻击 + 敏感输出"的通用护栏机制，
  本模块聚焦"最小权限 + PII"这一组安全实践。
