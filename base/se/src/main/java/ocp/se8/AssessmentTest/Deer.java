package ocp.se8.AssessmentTest;

/**
 *
 * DeerReindeer,true
 *
 */
public class Deer {

    public Deer() { System.out.print("Deer"); }

    public Deer(int age) { System.out.print("DeerAge");
    }

    public static void modifyValues(int num, String str) {
        num = 20; // 不会影响原始值
        str = "Modified"; // 不会影响原始引用
        System.out.println("方法内 - primitive: " + num + ", string: " + str);
    }

    private boolean hasHorns() { return false; }

    public static void main(String[] args) {
          Deer deer = new Reindeer(5);
          System.out.println(","+deer.hasHorns());
     }
}
 class Reindeer extends Deer {
     public Reindeer(int age) { System.out.print("Reindeer");
         age = 1;
     }
     public boolean hasHorns() { return true; }
 }