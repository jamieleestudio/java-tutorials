package com.yineng.bpe.synchronization;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SynchronizedInline {

    public  static final Object lock=new Object();

    public  static int c = 0;

    public void increase(){
        synchronized (lock){
            c++;
        }
    }

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (int i=0;i<100;i++){
            executor.submit(()-> new SynchronizedInline().increase());
        }
        executor.shutdown();
        //此处不能直接打印值，因为线程可能还未执行完成
        //System.out.println(SynchronizedInline.c);

        while (!executor.isTerminated()){
            //do nothing
        }
        System.out.println(SynchronizedInline.c);
    }

}
