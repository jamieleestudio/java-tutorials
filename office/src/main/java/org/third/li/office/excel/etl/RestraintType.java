package org.third.li.office.excel.etl;

public enum RestraintType {

    O,//可选
    M;

    public static RestraintType of(String type){
        for (RestraintType value : values()) {
            if(value.name().equals(type)){
                return value;
            }
        }
        throw new RuntimeException(" can not resolve type. the value is "+type);
    }

}
