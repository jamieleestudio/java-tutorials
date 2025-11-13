package ocp.se7.chapter2;

/**
 *
 * 当finally中有return 的时候，始终都会执行finally中的return
 *
 * @author lixf
 */
public class EHBehavior extends Thread {




    public void run(){
            Integer A = 0,B=  0;
            int r2 = A;
            B = 1;
            int r1 = B;
            A = 2;
            System.out.println(r2+"---"+r1);
    }


    public static void main(String []args) {
//        for(int i=0;i<10000;i++) {
//            new EHBehavior().start();
//        }


        try{
            System.out.println("11111111111111");
        }catch (Exception e){
            System.out.println("2222222222222222");
            throw new RuntimeException("hahahaha");
        }finally {
            System.out.println("33333333333333333333");
        }

//        try {
//            int i = 10/1; // LINE A
//
//            System.out.print("after throw -> ");
//            return;
//        } catch(ArithmeticException ae) {
//            System.out.print("in catch -> ");
//            return;
//        } finally {
//            System.out.print("in finally -> ");
//        }
//        System.out.print("after everything");
    }

}
