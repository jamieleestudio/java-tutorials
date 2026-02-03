# volatile（极度具体版）

## 你只要记住 3 句话

1. `volatile` 解决的是“看不见”和“乱序”，不解决“同时改”。
2. `volatile` 读 = 获取（acquire），`volatile` 写 = 释放（release）。
3. 只要出现“先读再写 / ++ / check-then-act / 复合条件”，大概率就不是 `volatile` 能单独扛住的。

## volatile 到底保证什么

### 1）可见性（Visibility）

一个线程写入 `volatile` 变量，另一个线程随后读取同一个 `volatile` 变量，一定能看到最新值。

对应 JMM 的语义：

- 对同一个变量的 `volatile` 写，happens-before 后续任何线程对同一个变量的 `volatile` 读。

### 2）有序性（Ordering）

`volatile` 会禁止“穿越”该读写操作的重排序：

- 在同一线程内：`volatile` 写之前的普通写，不能被重排到 `volatile` 写之后。
- 在同一线程内：`volatile` 读之后的普通读，不能被重排到 `volatile` 读之前。

把它当成：

- `volatile` 写：把前面的改动一并“发布”。
- `volatile` 读：把后面的读取建立在“已发布”的基础上。

## volatile 明确不保证什么

### 不保证原子性（Atomicity）

以下操作不是原子操作，即使变量是 `volatile`：

- `count++`
- `count = count + 1`
- `if (flag) { doSomething(); }`（读 + 分支 + 可能写）
- `if (value == 0) { value = 1; }`（check-then-act）

如果你需要“同时改”的正确性：

- 优先：`synchronized` / `ReentrantLock`
- 计数：`AtomicInteger` / `LongAdder`

## 什么时候用 volatile（直接给结论）

### 场景 A：停止标记 / 开关

- 一个线程改标记，另一个线程轮询标记。
- 标记本身不涉及复合更新。

运行示例：

```bash
cd concurrent/volatile
mvn -q -DskipTests=false test

java -cp target/classes com.yineng.bpe.volatiles.examples.VolatileVisibilityDemo
```

### 场景 B：发布“不可变对象”或“快照对象”的引用

把引用做成 `volatile`，每次更新都 new 一个新对象，然后一次性替换引用。

- 读方只读引用，不改对象内部状态。
- 写方用新对象替换引用。

运行示例：

```bash
java -cp target/classes com.yineng.bpe.volatiles.examples.VolatileReferencePublicationDemo
```

### 场景 C：DCL（双重检查锁）单例

`instance` 必须是 `volatile`。

运行示例：

```bash
java -cp target/classes com.yineng.bpe.volatiles.examples.VolatileDclSingletonDemo
```

## 什么时候不要用 volatile

### 1）计数 / 自增

`volatile int count` 不能让 `count++` 正确。

运行示例：

```bash
java -cp target/classes com.yineng.bpe.volatiles.examples.VolatileNotAtomicDemo
```

### 2）需要“一组字段一致性”的状态

例如：`x`、`y` 必须一起更新并且读到时必须匹配。此时 `volatile` 单字段做不到一致性。

可选方案：

- 用不可变对象 + `volatile` 引用（推荐）
- 或者加锁

## 你可以用它验证“乱序可见”的概率（示例性质）

该示例是概率型演示：不同机器/不同 JIT 状态可能表现不同。

```bash
java -cp target/classes com.yineng.bpe.volatiles.ReOrder
```

## volatile vs synchronized（一句话版）

- `volatile`：轻量，适合“一个线程写，多线程读”的简单状态发布；不提供互斥。
- `synchronized`：重一点，但提供互斥 + 可见性 + 有序性，能保证复合操作正确。

