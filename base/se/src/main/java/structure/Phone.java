package structure;


public class Phone {

    static {
        System.out.println("Phone static first");
    }

    public Phone(){
        System.out.println("Phone structure");
    }

    public Phone(int num){
        System.out.println("Phone structure"+num);
    }

    static {
        System.out.println("Phone static second");
    }

}
