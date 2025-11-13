package com.yineng.bpe.synchronization;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * synchronized 写在静态方法前面的时候，使用的是方法对应类的class对象作为lock
 *
 * tips:
 * 每个类都有且只有一个 Class 对象。运行程序时，jvm 首先检查类对应的 Class 对象是否已经加载。
 * 如果没有加载就会根据类名查找 .class 文件，并将其 Class 对象载入。
 * 虚拟机为每种类型管理一个独一无二的 Class 对象，但可以根据 Class 对象生成多个对象实例。某个类的 Class 对象被载入内存，它就会被用来创建这个类的所有对象
 */
public class SynchronizedStaticMethod {

    public  static int c = 0;

    /**
     * static 方法的 lock 为该方法的类所对应的class对象
     * 这里为SynchronizedStaticMethod.class
     */
    public  synchronized static void increase(){
        c++;
    }

    /**
     * 应为每次都是new 的对象，所以每个对象持有的lock 不一样
     */
    private void execute(){
        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (int i=0;i<100;i++){
            executor.submit(SynchronizedStaticMethod::increase);
        }
        executor.shutdown();
        while (!executor.isTerminated()){
            //do nothing
        }
        System.out.println(SynchronizedStaticMethod.c);
    }

    public static void main(String[] args) {
        System.out.println(SynchronizedStaticMethod.class);
        new SynchronizedStaticMethod().execute();
    }

}
