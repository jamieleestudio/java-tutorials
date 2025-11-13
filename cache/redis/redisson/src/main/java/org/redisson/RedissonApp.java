package org.redisson;

import org.redisson.api.RedissonClient;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RedissonApp {


    private static RedissonClient redisson = Redisson.create();

    public static int num = 0;

    public static void common(){
        redisson.getMap("users").put("li","lixiaofeng");
        redisson.getMap("users").forEach((k,v)->System.out.println(k+":"+v));

        redisson.getSet("books").addAll(List.of("java","python","javascript"));
        redisson.getSet("books").forEach(System.out::println);

        redisson.getList("teachers").addAll(List.of("zhangsan","lisi","wangwu"));
        redisson.getList("teachers").forEach(System.out::println);

    }

    public static void concurrentExistsSetListTryLockTime() {

        ExecutorService executor = Executors.newFixedThreadPool(10);

        for (int i=0;i<100;i++){
            executor.submit(()-> {
                var lock = redisson.getLock("lock1");
                try {
                    if(lock.tryLock(10, TimeUnit.SECONDS)){
                        try {
                            num++;
                        }finally {
                            lock.unlock();
                        }
                    }else {
                        System.out.println("系统繁忙");
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            });
        }

        executor.shutdown();
        while (!executor.isTerminated()){
            //do nothing
        }
        System.out.println("num:"+num);//
    }

    public static void concurrentExistsSetListTryLock(){

        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (int i=0;i<100;i++){
            executor.submit(()-> {
                var lock = redisson.getLock("lock1");
                if(lock.tryLock()){
                    try {
                        num++;
                    }finally {
                        lock.unlock();
                    }
                }else {
                    System.out.println("系统繁忙");
                }
            });
        }
        executor.shutdown();
        while (!executor.isTerminated()){
            //do nothing
        }
        System.out.println("num:"+num);//100 ?
    }

    public static void concurrentExistsSetListLock(){

        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (int i=0;i<100;i++){
            executor.submit(()-> {
                var lock = redisson.getLock("lock1");
                lock.lock();
                try {
                    num++;
                }finally {
                    lock.unlock();
                }
            });
        }
        executor.shutdown();
        while (!executor.isTerminated()){
            //do nothing
        }
        System.out.println("num:"+num);//100
    }

    public static void concurrentExistsSetList(){

        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (int i=0;i<100;i++){
            executor.submit(()-> {
                //存在并发问题
                var flag = redisson.getBucket("flag");
                //并发情况下，以下代码执行多次
                if(!flag.isExists()){
                    System.out.println("flag not exists.");
                    flag.set("exist.");
                }
            });
        }
    }


    public static void concurrentGetContainsAddList(){
        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (int i=0;i<100;i++){
            executor.submit(()-> {
                var books = redisson.getList("teachers");
                //存在并发问题
                if(!books.contains("go")){
                    System.out.println("book size:"+books.size());
                    books.add("go");
                }else{
                    System.out.println("book size:"+books.size());
                }
            });
        }
    }


    public static void main(String[] args) {
//        concurrentExistsSetList();
//        concurrentExistsSetListLock();
//        concurrentExistsSetListTryLock();
        concurrentExistsSetListTryLockTime();
    }

}
