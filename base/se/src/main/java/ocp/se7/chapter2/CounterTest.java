package ocp.se7.chapter2;

/**
 * @author lixf
 */
class SimpleCounter<T> {
    private static int count = 0;
    public SimpleCounter() {
        count++;
    }
    static int getCount() {
        return count;
    }
}
class CounterTest {

    public static void main(String []args) {

        new CounterTest();

        SimpleCounter<Double>  doubleCounter = new SimpleCounter<Double>();
        SimpleCounter<Integer> intCounter = null;
        SimpleCounter rawCounter = new SimpleCounter(); // RAW
        System.out.println("SimpleCounter<Double> counter is "  + doubleCounter.getCount());
        System.out.println("SimpleCounter<Integer> counter is " + intCounter.getCount());
        System.out.println("SimpleCounter counter is " + rawCounter.getCount());
    }
}
