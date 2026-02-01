package com.yineng.bpe;

public class Main {
    public static void main(String[] args) {
        System.out.println("Concurrent tools demos start");
        CountDownLatchExample.run();
        CyclicBarrierExample.run();
        SemaphoreExample.run();
        PhaserExample.run();
        ExchangerExample.run();
        SynchronousQueueExample.run();
        BlockingQueuePipelineExample.run();
        ReadWriteLockExample.run();
        AtomicIntegerExample.run();
        System.out.println("Concurrent tools demos end");
    }
}
