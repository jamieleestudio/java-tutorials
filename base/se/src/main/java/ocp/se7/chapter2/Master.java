package ocp.se7.chapter2;

/**
 * @author lixf
 */
class Worker extends Thread {
    public void run() {
        System.out.println(Thread.currentThread().getName());
    }
}
class Master {
    public static void main(String []args) throws InterruptedException {
        Thread.currentThread().setName("Master ");
        Thread worker = new Worker();
        worker.setName("Worker ");
        worker.start();
        Thread.currentThread().join();
        System.out.println(Thread.currentThread().getName());
    }
}
