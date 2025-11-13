package ocp.se7.chapter2;

/**
 *
 * 静态方法和属性是属于类的，调用的时候直接通过类名.方法名完成，不需要继承机制就可以调用。
 * 如果子类里面定义了静态方法和属性，那么这时候父类的静态方法或属性称之为"隐藏"。
 * 如果你想要调用父类的静态方法和属性，直接通过父类名.方法或父类名.变量名完成，至于是否继承一说，子类是有继承静态方法和属性，但是跟实例方法和属性不太一样，存在"隐藏"的这种情况
 *
 * 非静态方法可以被继承和重写，因此可以实现多态
 *
 * @author lixf
 */
public class OverrideTest {


    public static void main(String []args) {
       new OverrideTest().test();
    }

    public void test(){
        Base bObj = new Derived();
        bObj.foo(bObj);
        //In Base.foo()
        //In Derived.bar()
    }
}


 class Base {

    public static String s = "A";

    public static void foo(Base bObj) {
        System.out.println("In Base.foo()");
        bObj.bar();
    }

    public void bar() {
        System.out.println("In Base.bar()");
    }

}

 class Derived extends Base {

     public static String s = "B";


     public static void foo(Base bObj) {
        System.out.println("In Derived.foo()");
        bObj.bar();
    }

    public void bar() {
        System.out.println("In Derived.bar()");
    }

}
