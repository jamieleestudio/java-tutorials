package ocp.se7.chapter2;

import java.util.regex.Pattern;

/**
 * @author lixf
 */
public class Regex {

    public static void main(String []args) {
        String pattern = "a*b+c{3}";
        String []strings = { "abc", "abbccc", "aabbcc", "aaabbbccc" };
        for(String str : strings) {
            System.out.print(Pattern.matches(pattern, str) + " ");
        }
    }

}
