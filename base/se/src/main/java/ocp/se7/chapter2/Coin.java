package ocp.se7.chapter2;



/**
 * @author lixf
 */
 class Coin {

    public static void overload(Head side) { System.out.print(side.getSide()); }
    public static void overload(Tail side) { System.out.print(side.getSide()); }
    public static void overload(Side side) { System.out.print("Side "); }
    public static void overload(Object side) { System.out.print("Object "); }

    public static void main(String []args) {
        Side firstAttempt = new Head();
        Tail secondAttempt = new Tail();
        //Side
        overload(firstAttempt);
        //Object
        overload((Object)firstAttempt);
        //Tail
        overload(secondAttempt);
        //Side
        overload((Side)secondAttempt);
    }

}

 class Head implements Side{

    public String getSide() { return "Head "; }
}

 interface Side {

    String getSide();
}

 class Tail implements Side{
    public String getSide() { return "Tail "; }

}
