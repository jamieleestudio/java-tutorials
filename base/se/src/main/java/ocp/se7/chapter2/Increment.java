package ocp.se7.chapter2;

/**
 * @author lixf
 */
public class Increment {

    public static void main(String []args) {
        Integer i = 10;
        Integer j = 11;
        Integer k = ++i; // INCR
        //++ 在前，先递增再加1，++在后，先运算再加1
        System.out.println("k == j is " + (k == j));
        System.out.println("k.equals(j) is " + k.equals(j));
    }

}
