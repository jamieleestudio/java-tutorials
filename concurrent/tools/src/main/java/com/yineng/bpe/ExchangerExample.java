package com.yineng.bpe;

import java.util.concurrent.Exchanger;

public class ExchangerExample {
    public static void run() {
        Exchanger<String> exchanger = new Exchanger<>();
        Thread t1 = new Thread(() -> {
            try {
                String r = exchanger.exchange("A");
                System.out.println("Exchanger t1 got " + r);
            } catch (Exception e) {
                System.out.println("Exchanger t1 error=" + e.getClass().getSimpleName() + ":" + e.getMessage());
            }
        });
        Thread t2 = new Thread(() -> {
            try {
                String r = exchanger.exchange("B");
                System.out.println("Exchanger t2 got " + r);
            } catch (Exception e) {
                System.out.println("Exchanger t2 error=" + e.getClass().getSimpleName() + ":" + e.getMessage());
            }
        });
        t1.start();
        t2.start();
        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            System.out.println("Exchanger join error=" + e.getClass().getSimpleName() + ":" + e.getMessage());
        }
    }
}
