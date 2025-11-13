package algorithm;

import java.util.Queue;
import java.util.StringJoiner;
import java.util.concurrent.ArrayBlockingQueue;

/**
 *
 *
 * 用队列实现栈
 *
 * @author lixf
 */
public class ListStack<T> {

    public static void main(String[] args) {
        ListStack<String> stringListStack = new ListStack<>(new ArrayBlockingQueue<>(100));
        //2
        //1
        stringListStack.push("1");
        stringListStack.push("2");
        // 2
        System.out.println(stringListStack.pop());
        //3
        //1
        stringListStack.push("3");

        //3
        System.out.println(stringListStack.pop());
        //1
        System.out.println(stringListStack.pop());
        System.out.println(stringListStack);

    }

    private final Queue<T> queue;

    public ListStack(Queue<T> queue){
        this.queue = queue;
    }

    //入
    public void push(T t){
        queue.add(t);
    }

    public String toString(){
        StringJoiner stringJoiner = new StringJoiner(",");
        for (T t : queue) {
            stringJoiner.add(String.valueOf(t));
        }
        return "["+stringJoiner+"]";
    }

    //出
    public T pop(){
        int lastIndex = queue.size()-1;
        for(int i=0;i<=lastIndex;i++){
            T t = queue.remove();
            if(i < lastIndex){
                queue.add(t);
            }else {
                return t;
            }
        }
        return null;
    }

}
