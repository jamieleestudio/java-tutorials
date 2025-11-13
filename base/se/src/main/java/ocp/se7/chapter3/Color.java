package ocp.se7.chapter3;

/**
 * @author lixf
 */
class Color {
    int red, green, blue;

    //构造方法没有返回值,这里有void 有返回值说明不是构造方法
    void Color() {
        red = 10;
        green = 10;
        blue = 10;
    }

    void printColor() {
        System.out.println("red: " + red + " green: " + green + " blue: " + blue);
    }

    public static void main(String [] args) {
        Color color = new Color();
        color.printColor();
        // red:0 green:0 blue:0
    }
}
