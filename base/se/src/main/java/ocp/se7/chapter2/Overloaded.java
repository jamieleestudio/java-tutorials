package ocp.se7.chapter2;

/**
 * @author lixf
 */
public class Overloaded {

    public static void foo(Integer i) { System.out.println("foo(Integer)"); }

    public static void foo(byte i) { System.out.println("foo(byte)"); }
    public static void foo(short i) { System.out.println("foo(short)"); }
    public static void foo(int i) { System.out.println("foo(int)"); }
    public static void foo(long i) { System.out.println("foo(long)"); }
    public static void foo(float i) { System.out.println("foo(float)"); }
    public static void foo(double i) { System.out.println("foo(double)"); }
    public static void foo(int ... i) { System.out.println("foo(int ...)"); }
    public static void main(String []args) {
        var a  = 1.1;
        foo(a);

//        String zh = "你好";
//        System.out.println(zh.getBytes(Charset.forName("UTF-32")).length);
//        System.out.println(zh.getBytes(Charset.forName("UTF-8")).length);
//        System.out.println(zh.getBytes(Charset.forName("UTF-16")).length);
//        System.out.println(zh.getBytes(Charset.forName("GBK")).length);
//        System.out.println(zh.length());
//        System.out.println(zh.getBytes().length);;
//        //查看当前系统的字符编码方式
//        System.out.println(Charset.defaultCharset().name());
//        //查看当前系统的编码方式
//        System.out.println(System.getProperty("file.encoding"));

    }

}
