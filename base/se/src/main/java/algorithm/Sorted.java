package algorithm;


import java.util.Arrays;

/**
 *
 * 排序算法
 *
 * @author lixf
 */
public class Sorted {


    /**
     *
     * 假设前面 n-1(其中 n>=2)个数已经是排好顺序的，现将第 n 个数插到前面已经排好的序列中，然后找到合适自己的位置，使得插入第n个数的这个序列也是排好顺序的。
     * 按照此法对所有元素进行插入，直到整个序列排为有序的过程，称为插入排序。
     * 插入排序
     */
    public static  void  insertion(int [] arr){

        for(int i = 1 ;i<arr.length;i++){
            int key = arr[i];
            //前面一个值
            for( int j= 0; j<= i;j++){
                if(key < arr[j]){
                    int temp = arr[i];
                    arr[i] = arr[j];
                    arr[j] = temp;
                }
            }
        }
        System.out.println(Arrays.toString(arr));
    }

    /**
     * 冒泡排序
     */
    public static void bubble(int [] arr){

        for(int i = 0 ;i<arr.length;i++){

            for( int j = 0 ;j< arr.length - i-1 ;j++){

                if(arr[j] > arr[j+1]){
                    int temp = arr[j];
                    arr[j] = arr[j+1];
                    arr[j+1] = temp;
                }

            }
        }
        System.out.println(Arrays.toString(arr));
    }

    /**
     * 选择排序
     *
     * 每次把未查找部分的最小的数放在前面。
     *
     */
    public static void selection(int [] arr){

        for(int i = 0 ;i<arr.length;i++){
            int index = 0;
            int min = arr[i];//将最小值存下来
            // int j= i 每次在后面为查找的部分开始勋在
            for( int j = i ;j< arr.length ;j++){
                if(min > arr[j]){
                    min = arr[j];
                    index = j;
                }
            }
            System.out.println(min);
            int temp = arr[i];
            arr[i] = min;
            arr[index] = temp;
        }
        System.out.println(Arrays.toString(arr));
    }

    public static void quick(int [] arr){

    }

    public static void main(String[] args) {
//        insertion(new int[]{8,1,3,6,3,2,9});
//        bubble(new int[]{8,1,3,6,3,2,9});
        selection(new int[]{8,1,3,6,3,2,9});
    }


}
