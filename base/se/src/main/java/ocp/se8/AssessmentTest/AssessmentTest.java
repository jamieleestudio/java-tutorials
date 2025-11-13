package ocp.se8.AssessmentTest;

 public class AssessmentTest {

 private static int $;

 public static void main(String[] main) {

        String a_b;
        //int 默认值是0
        System.out.println($);
//      System.out.print(a_b);

     System.out.println(2%3);

     String s1 = "Java";
     String s2 = "Java";
     StringBuilder sb1 = new StringBuilder();
     sb1.append("Ja").append("va");
     System.out.println(s1 == s2);
     System.out.println(s1.equals(s2));
     System.out.println(sb1.toString() == s1);
     System.out.println(sb1.toString().equals(s1));
    }

     interface HasTail { int getTailLength(); }

     abstract class Puma implements HasTail {
//          int getTailLength() {return 4;}
     }

 }