package org.third.li.office.excel.etl;

public class TableDmlInsertResolverFactory {


    public static TableDmlResolver  getResolver(String fileName){
        if(fileName.contains("明细") || fileName.contains("DWD") || fileName.contains("数据中台")){
            return new DwdTableDmlInsertResolver();
        }else if(fileName.contains("智慧") || fileName.contains("数据类")){
            return new AdsTableDmlInsertResolver();
        }
        throw new RuntimeException("con not support resolver of "+fileName);
    }

}
