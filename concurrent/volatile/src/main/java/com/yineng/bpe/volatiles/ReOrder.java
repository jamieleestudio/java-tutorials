package com.yineng.bpe.volatiles;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 *
 * Original code Valid compiler transformation
 *    Initially, A == B == 0
 *
 *    Thread 1     Thread 2
 *      1: r2 = A;   3: r1 = B
 *      2: B = 1;    4: A = 2
 *    May observe r2 == 2, r1 == 1
 *
 * 程序中用到了局部变量 r1 和 r2，以及共享变量 A 和 B。可能
 * 会出现 r2 == 2、r1 == 1 这样的结果。直觉上，应当要么指令 1 先执行要么指令 3
 * 先执行。如果指令 1 先执行，它不应该能看到指令 4 中写入的值。如果指令 3 先执
 * 行，它不应该能看到指令 2 写的值
 *
 *    Initially, A == B == 0
 *     Thread 1    Thread 2
 *     B = 1;      A = 2
 *     r2 = A;     r1 = B
 * May observe r2 == 2, r1 == 1
 * Figure 1: Surprising results caused by statement reordering
 *
 * <p></p>
 * This program contains local variables r1 and r2; it
 * also contains <b>shared variables A and B</b>, which are fields of an object
 *
 * 本地变量r1和r2
 * 共享变量A和B
 *
 */
public class ReOrder {

    private static int A=0;
    private static int B=0;

    public static void main(String[] args) {

        ExecutorService executor = Executors.newFixedThreadPool(2);

        long surprises = 0;
        long iterations = 200_000;
        for (long i = 0; i < iterations; i++) {
            A = 0;
            B = 0;

            Future<Integer> t1 = executor.submit(() -> {
                int r2 = A;
                B = 1;
                return r2;
            });

            Future<Integer> t2 = executor.submit(() -> {
                int r1 = B;
                A = 2;
                return r1;
            });

            try {
                int r2 = t1.get();
                int r1 = t2.get();
                if (r2 == 2 && r1 == 1) {
                    surprises++;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("iterations = " + iterations);
        System.out.println("surprises  = " + surprises);
    }

}
