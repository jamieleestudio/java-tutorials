package ocp.se7.chapter2;

/**
 * @author lixf
 */
public class PenguinTest {

    public static void main(String []args) {
        Penguin pingu = new Penguin();
        pingu.walk();
        pingu.fly();
        //walk
        //cannot fly
    }

}


class CannotFlyException extends Exception {}

interface Birdie {
    public abstract void fly() throws CannotFlyException;
}
interface Biped {
    public void walk();
}
abstract class NonFlyer {
    public void fly() { System.out.print("cannot fly "); } // LINE A
}
class Penguin extends NonFlyer implements Birdie, Biped { // LINE B
    public void walk() { System.out.print("walk "); }
}
