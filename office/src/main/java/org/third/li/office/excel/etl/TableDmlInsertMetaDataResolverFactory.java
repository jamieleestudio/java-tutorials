package org.third.li.office.excel.etl;

public class TableDmlInsertMetaDataResolverFactory {


    public static TableDmlInsertMetaDataResolver  getResolver(String fileName){
        if(fileName.contains("明细") || fileName.contains("数据中台")){
            return new DwdTableDmlInsertMetaDataResolver();
        }else if(fileName.contains("智慧") || fileName.contains("数据类")){
            return new AdsTableDmlInsertMetaDataResolver();
        }
        throw new RuntimeException("con not support resolver of "+fileName);
    }

}
