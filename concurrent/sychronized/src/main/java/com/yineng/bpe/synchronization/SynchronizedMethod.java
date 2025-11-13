package com.yineng.bpe.synchronization;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * synchronized 写在方法前面的时候，使用的是对象实例作为lock
 *
 */
public class SynchronizedMethod {

    public  static int c = 0;

    public  synchronized void increase(){
        c++;
    }

    /**
     * 应为每次都是new 的对象，所以每个对象持有的lock 不一样
     */
    private void executeMultipleInstance(){
        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (int i=0;i<100;i++){
            executor.submit(()-> new SynchronizedMethod().increase());
        }
        executor.shutdown();
        while (!executor.isTerminated()){
            //do nothing
        }
        System.out.println(SynchronizedMethod.c);
    }

    private static void clear(){
        c = 0;
    }

    /**
     * 这里只new 了一个对象，只有一把锁
     */
    private void executeSingleInstance(){
        var m = new SynchronizedMethod();
        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (int i=0;i<100;i++){
            executor.submit(m::increase);
        }
        executor.shutdown();
        while (!executor.isTerminated()){
            //do nothing
        }
        System.out.println(SynchronizedMethod.c);
    }


    public static void main(String[] args) {
        new SynchronizedMethod().executeMultipleInstance();
        SynchronizedMethod.clear();
        new SynchronizedMethod().executeSingleInstance();
    }

}
