package ocp.se7.chapter2;

/**
 *
 * null  不是 Object 类型
 * 不是对象，也不知道什么类型，也不是java.lang.Object的实例
 * @author lixf
 */
public class NullInstanceof {

    public static void main(String []args) {
        String str = null;
        if(str instanceof Object) // NULLCHK
            System.out.println("str is Object");
        else
            System.out.println("str is not Object");
    }

}
