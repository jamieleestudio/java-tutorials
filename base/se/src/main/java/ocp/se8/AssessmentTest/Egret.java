package ocp.se8.AssessmentTest;

import java.util.function.Predicate;

public class Egret {
     private String color;

     public Egret() {
             this("white");
     }
     public Egret(String color) {
             color = color;
     }
     public static void main(String[] args) {
             Egret e = new Egret();
             System.out.println("Color:" + e.color);

         System.out.println(test(i -> i == 150));
//         System.out.println(test(i -> {i == 5;}));
         System.out.println(test((i) -> i == 5));
//         System.out.println(test((int i) -> i == 5);
//         System.out.println(test((int i) -> {return i == 5;}));
         System.out.println(test((i) -> {return i == 5;}));
     }

      private static boolean test(Predicate<Integer> p) {
         return p.test(150);
      }

 }