package ocp.se7.chapter2;

/**
 *
 * 方法签名由方法名称和一个参数列表（方法的参数的顺序和类型）组成。
 * 注意，方法签名不包括方法的返回类型。不包括返回值和访问修饰符。
 * 常见的问题应用：重载和重写
 *
 * 父类：如果是private，那么子类就不存在重写，只是新建了个方法
 *
 * @author lixf
 */
class InvalidValueException extends IllegalArgumentException {}
class InvalidKeyException extends IllegalArgumentException {}
class BaseClass {
    void foo() throws IllegalArgumentException {
        throw new IllegalArgumentException();
    }
}
class DeriClass extends BaseClass {
    public void foo() throws IllegalArgumentException {
        throw new InvalidValueException();
    }
}
class DeriDeriClass extends DeriClass {
    public void foo() { // LINE A
        throw new InvalidKeyException();
    }
}
class EHTest {
    public static void main(String []args) {
        try {
            BaseClass base = new DeriDeriClass();
            base.foo();
        } catch(RuntimeException e) {
            System.out.println(e);
        }
    }
}
