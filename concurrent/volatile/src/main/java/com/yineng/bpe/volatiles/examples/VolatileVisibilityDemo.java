package com.yineng.bpe.volatiles.examples;

import java.util.concurrent.TimeUnit;

public class VolatileVisibilityDemo {

    private static boolean running = true;
    private static volatile boolean volatileRunning = true;

    public static void main(String[] args) throws Exception {
        System.out.println("case1: plain boolean stop flag");
        runPlainFlag();
        System.out.println();

        System.out.println("case2: volatile boolean stop flag");
        runVolatileFlag();
    }

    private static void runPlainFlag() throws Exception {
        running = true;
        Thread t = new Thread(() -> {
            while (running) {
            }
            System.out.println("plain flag worker stopped");
        });
        t.start();

        TimeUnit.MILLISECONDS.sleep(200);
        running = false;

        t.join(500);
        if (t.isAlive()) {
            System.out.println("plain flag worker is still running (possible visibility issue)");
            t.interrupt();
        }
    }

    private static void runVolatileFlag() throws Exception {
        volatileRunning = true;
        Thread t = new Thread(() -> {
            while (volatileRunning) {
            }
            System.out.println("volatile flag worker stopped");
        });
        t.start();

        TimeUnit.MILLISECONDS.sleep(200);
        volatileRunning = false;

        t.join(500);
        if (t.isAlive()) {
            System.out.println("volatile flag worker is still running (unexpected)");
            t.interrupt();
        }
    }
}

