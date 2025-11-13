package com.yineng.bpe.synchronization;

public class SynchronizedDoubleCheck {

    //volatile 可见性，一个线程对变量的修改对另一个线程可见
    private volatile static SynchronizedDoubleCheck singleSynchronizedDoubleCheck;

    private SynchronizedDoubleCheck(){}

    public static SynchronizedDoubleCheck getInstance(){
        if(singleSynchronizedDoubleCheck == null){
            synchronized (SynchronizedDoubleCheck.class){
                if(singleSynchronizedDoubleCheck == null){
                    singleSynchronizedDoubleCheck = new SynchronizedDoubleCheck();
                }
            }
        }
        return singleSynchronizedDoubleCheck;
    }

    public static void main(String[] args) {
        System.out.println(SynchronizedDoubleCheck.getInstance() == SynchronizedDoubleCheck.getInstance());
    }

}
