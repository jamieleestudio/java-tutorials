package com.yineng.bpe.virtualthread;

/**
 * 作用域值 (Scoped Values) 示例 (JDK 21 预览特性)
 * 
 * Scoped Values (JEP 446) 是 ThreadLocal 的现代替代品，特别适合与虚拟线程结合使用。
 * 
 * 核心优势：
 * 1. 单向数据传递（不可变性）：值绑定后不可修改，仅在指定的作用域（run/call 的闭包）内有效。
 * 2. 避免内存泄漏：当 run/call 方法执行完毕后，绑定的值会自动失效并被垃圾回收，无需手动 remove()。
 * 3. 极低的开销：在海量虚拟线程并发时，ScopedValue 的内存和性能开销远低于 ThreadLocal。
 * 
 * 注意：在 JDK 21 中属于预览特性，编译和运行时需要开启 `--enable-preview` 参数。
 */
public class ScopedValueDemo {

    // 1. 定义一个 ScopedValue，通常定义为 public static final
    public static final ScopedValue<String> USER_CONTEXT = ScopedValue.newInstance();

    public static void main(String[] args) throws Exception {
        System.out.println("=== ScopedValue (作用域值) 示例 ===\n");

        // 模拟 Web 请求 1：绑定用户 "Alice"
        Thread vThread1 = Thread.ofVirtual().name("request-alice").start(() -> {
            // 使用 ScopedValue.where 绑定值，并在 run 的闭包内生效
            ScopedValue.where(USER_CONTEXT, "Alice").run(() -> {
                handleRequest("GET /api/user");
            });
        });

        // 模拟 Web 请求 2：绑定用户 "Bob"
        Thread vThread2 = Thread.ofVirtual().name("request-bob").start(() -> {
            ScopedValue.where(USER_CONTEXT, "Bob").run(() -> {
                handleRequest("POST /api/order");
            });
        });

        // 模拟主线程尝试获取（不在作用域内，将会报错或获取不到，可通过 isBound 判断）
        System.out.println("主线程是否绑定了 USER_CONTEXT? " + USER_CONTEXT.isBound());

        // 等待虚拟线程执行完毕
        vThread1.join();
        vThread2.join();
        
        System.out.println("\n所有请求处理完毕。");
    }

    /**
     * 模拟控制层/业务层方法
     */
    private static void handleRequest(String requestPath) {
        // 直接从 ScopedValue 获取当前上下文的值，无需通过方法参数层层传递
        String currentUser = USER_CONTEXT.get();
        System.out.printf("[%s] 接收到请求: %-15s | 当前用户: %s%n", 
                Thread.currentThread().getName(), requestPath, currentUser);
        
        // 模拟调用更深层的持久化方法
        saveToDatabase();
    }

    /**
     * 模拟数据访问层方法
     */
    private static void saveToDatabase() {
        // 在深层调用链中依然可以安全、高效地读取到 ScopedValue
        String auditUser = USER_CONTEXT.get();
        System.out.printf("[%s] 保存数据成功...          | 审计操作人: %s%n", 
                Thread.currentThread().getName(), auditUser);
    }
}
