
# AssessmentTest
1，string 类型需要初始化后再使用
~~~ java
        1.String a_b;
        2.System.out.print(a_b);
~~~
第二行编译错误
****

2，接口的方法是public 的修饰符，他的实现类必须实现这个方法，并且不能缩小访问范围
~~~
     interface HasTail { int getTailLength(); }
     
     abstract class Puma implements HasTail {
        protected int getTailLength() {return 4;}
     }
~~~ 
`getTailLength()` 方法不能被声明为 `protected`

****
3，方法的引用传递和值传递  
Java 中所有参数都是通过值传递的，基本类型（int, double, boolean 等）：传递的是实际值的副本，引用类型（对象）：传递的是对象引用的副本（即内存地址的副本）
~~~
    public static void addToInt(int x, int amountToAdd) {
         x = x + amountToAdd;
         }
        public static void main(String[] args) {
         int a = 15;
         int b = 10;
         MathFunctions.addToInt(a, b);
         System.out.println(a);
    }
~~~
Java 始终是值传递：传递的是引用的副本（值），而不是引用本身。 通过引用副本可以修改实际对象：因为副本和原始引用指向同一个对象。
重新分配引用副本不影响原始引用：因为只是改变了副本的指向
****
4，在创建子类实例时，**父类的构造方法都保证会被执行一次**，以确保父类中定义的字段和状态能得到正确的初始化。这是继承体系能够正常工作的基石。


****
5，子类实现两个接口相同得 default 方法，则子类必须实现一个，因为default方法为具体实现，子类不覆盖，就不知道调用接口得方法
如果去掉default就没问题
~~~
     public interface Animal { public default String getName() { return null; } }
     public interface Mammal { public default String getName() { return null; } }
     abstract class Otter implements Mammal, Animal {}
~~~

****
6，Java对-128到127之间的Integer对象进行了缓存
~~~
        Integer i = 5; // 自动装箱：int -> Integer
        
        // 比较1：Integer == int (自动拆箱)
        System.out.println("i == 5: " + (i == 5)); // true
        
        // 比较2：Integer == Integer (对象引用比较)
        Integer j = 5;
        System.out.println("i == j: " + (i == j)); // true (由于Integer缓存)
        
        Integer k = 200;
        Integer l = 200;
        System.out.println("k == l: " + (k == l)); // false (超出缓存范围)
        
        // 正确比较Integer对象的方式
        System.out.println("k.equals(l): " + k.equals(l)); // true
~~~

# chapter 1

1，0b 开头表示二进制，0开头表八进制，0x开头表示十六进制
~~~
     System.out.println(0b11);
     System.out.println(015);
     System.out.println(0x15);
~~~