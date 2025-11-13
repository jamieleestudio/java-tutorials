package ocp.se7.chapter2;

/**
 * @author lixf
 */
public class Point2D {

    private int x, y;
    public Point2D(int x, int y) {
        x = x;
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + "]";
    }

    public static void main(String []args) {
        Point2D point = new Point2D(10, 20);

        //[0, 0]
        //这里构造方法没有指定this.x,所以 x,y的值还是为基本类型int 的默认值0
        System.out.println(point);
    }
}

