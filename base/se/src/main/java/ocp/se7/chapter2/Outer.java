package ocp.se7.chapter2;

/**
 * @author lixf
 */
public class Outer {

    static class Inner {
        public final String text = "Inner";
    }

}
class InnerClassAccess {
    public static void main(String []args) {
        System.out.println(/*CODE HERE*/);
        System.out.println(new Outer.Inner().text);
    }
}
