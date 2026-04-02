package com.yineng.bpe.virtualthread;

/**
 * 虚拟线程基础创建方式示例
 * 要求：JDK 21+
 */
public class BasicVirtualThreadDemo {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 虚拟线程基础创建示例 ===");

        // 方式一：使用 Thread.ofVirtual().start() 直接启动虚拟线程
        Thread vThread1 = Thread.ofVirtual().start(() -> {
            System.out.println("方式一: Thread.ofVirtual().start() -> " + Thread.currentThread());
        });

        // 方式二：使用 Thread.ofVirtual().unstarted() 创建但不立即启动
        Thread vThread2 = Thread.ofVirtual().unstarted(() -> {
            System.out.println("方式二: Thread.ofVirtual().unstarted() 然后 start() -> " + Thread.currentThread());
        });
        vThread2.start();

        // 方式三：使用 Thread.startVirtualThread() (最简洁的语法)
        Thread vThread3 = Thread.startVirtualThread(() -> {
            System.out.println("方式三: Thread.startVirtualThread() -> " + Thread.currentThread());
        });

        // 方式四：使用 Thread.Builder 批量创建带前缀名的虚拟线程
        Thread.Builder.OfVirtual builder = Thread.ofVirtual().name("my-vthread-", 1);
        Thread vThread4 = builder.start(() -> {
            System.out.println("方式四: 使用 Builder 创建带名字的虚拟线程 -> " + Thread.currentThread());
        });

        // 确保主线程等待虚拟线程执行完毕
        vThread1.join();
        vThread2.join();
        vThread3.join();
        vThread4.join();

        System.out.println("所有虚拟线程执行完毕。");
    }
}
