package com.yineng.bpe;

import java.util.concurrent.FutureTask;

public class FutureTaskExample {
    public static void run() {
        FutureTask<String> ft = new FutureTask<>(() -> "future-task");
        Thread t = new Thread(ft);
        t.start();
        try {
            String r = ft.get();
            System.out.println("FutureTaskExample result=" + r);
        } catch (Exception e) {
            System.out.println("FutureTaskExample error=" + e.getClass().getSimpleName() + ":" + e.getMessage());
        }
    }
}
