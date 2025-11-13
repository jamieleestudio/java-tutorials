package org.third.li.office.excel.etl;

import java.util.Arrays;
import java.util.Map;

public interface TableDmlInsertMetaDataResolver {


    TableMetaData toTableMetaData(GenericTableSource excelTableSource);


    default Integer getIndex(Map<String,Integer> headerIndex, String ... columns){
        for (String column : columns) {
            var index = headerIndex.get(column);
            if(index != null){
                return index;
            }
        }
        throw  new RuntimeException(" can not find column from :"+ Arrays.toString(columns));
    }


}
