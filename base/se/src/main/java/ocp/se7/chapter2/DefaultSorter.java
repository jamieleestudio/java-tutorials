package ocp.se7.chapter2;

import java.util.Arrays;

/**
 * @author lixf
 */
public class DefaultSorter {

    public static void main(String[] args) {
        String[] brics = {"Brazil", "Russia", "India", "China"};
        Arrays.sort(brics, null); // LINE A
        for(String country : brics) {
            System.out.print(country + " ");
        }
    }

}
