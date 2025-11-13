package concurrent;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockTest {

    private static final ReentrantLock reentrantLock = new ReentrantLock();
    static int i = 0;


    public static void main(String[] args) {

         Set<String> set = new HashSet<>();
         set.add("1");

        for(int j=0;j<10000;j++){

            new Thread(() -> {
//                ReentrantLock reentrantLock = new ReentrantLock();
                reentrantLock.lock();
                try {
                    if(set.contains("1")){
                        System.out.println("lalalal");
                        set.remove("1");
                    }
                }finally {
                    reentrantLock.unlock();
                }
            }).start();
        }
        System.out.println(set.size());

    }

}
