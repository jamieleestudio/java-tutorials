package ocp.se7.chapter2;

/**
 * @author lixf
 */
public class StrEqual {

    public static void main(String []args) {
        String s1 = "hi";
        String s2 = new String("hi");
        String s3 = "hi";
        if(s1 == s2) {
            System.out.println("s1 and s2 equal");
        } else {
            System.out.println("s1 and s2 not equal");
        }

        if(s1 == s3) {
            System.out.println("s1 and s3 equal");
        } else {
            System.out.println("s1 and s3 not equal");
        }
    }

}
