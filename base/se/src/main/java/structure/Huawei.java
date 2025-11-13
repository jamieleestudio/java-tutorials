package structure;

/*
 *
 * @author Likasi
 */
public class Huawei extends Phone{


    static Phone phone3 = new Phone(3);
    static Phone phone2 = new Phone(2);

    static {
        System.out.println("Huawei static first");
    }

    public Huawei(){
        System.out.println("Huawei structure");
    }

    public Huawei(int num){
        System.out.println("Huawei structure"+num);
    }

    static {
        System.out.println("Huawei static second");
    }

}
