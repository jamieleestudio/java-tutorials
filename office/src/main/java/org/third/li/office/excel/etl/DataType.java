package org.third.li.office.excel.etl;

public enum DataType {

    C,N,T, TI, DT, TY;
    public static DataType of(String type){
        for (DataType value : values()) {
            if(value.name().equals(type)){
                return value;
            }
        }
        throw new RuntimeException(" can not resolve type. the value is "+type);
    }

}
