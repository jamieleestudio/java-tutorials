package ocp.se7.chapter2;

enum Cards { CLUB, SPADE, DIAMOND, HEARTS };
class CardsEnumTest {
    public static void main(String []args) {
        /* TRAVERSE */

        for(Cards cards : Cards.values()){
            System.out.print(cards + " ");
        }

    }
}
