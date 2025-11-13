package org.third.li.office.excel.etl;

public class TableDdlCreateResolverFactory {


    public static TableDdlResolver  getResolver(String fileName){
        if(fileName.contains("DWD") || fileName.contains("明细")){
           return new DwdTableDdlCreateResolver();
        }else if(fileName.contains("ADS") || fileName.contains("ZYG")){
            return new AdsTableDdlCreateResolver();
        }
        throw new RuntimeException("con not support resolver of "+fileName);
    }

}
