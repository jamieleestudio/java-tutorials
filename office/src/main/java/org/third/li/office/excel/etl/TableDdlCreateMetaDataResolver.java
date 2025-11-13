package org.third.li.office.excel.etl;

import java.util.*;

public class TableDdlCreateMetaDataResolver implements  TableDmlInsertMetaDataResolver{

    @Override
    public TableMetaData toTableMetaData(GenericTableSource excelTableSource) {
        GenericTableMetaData tableMetaData  = new GenericTableMetaData();

        var tableInfoArr =  excelTableSource.getName().split("-");
        if(tableInfoArr.length < 2){
            var tableName  = PinyinUtils.getFirstLetter(tableInfoArr[0]);
            tableMetaData.setTableName(tableName);
            tableMetaData.setDescription(tableInfoArr[0]);
        }else{
            tableMetaData.setTableName(tableInfoArr[0]);
            tableMetaData.setDescription(tableInfoArr[1]);
        }
        Map<String,Integer> headerIndex = new HashMap<>();
        int i=0;
        for (String header : excelTableSource.getHeaders()) {
            headerIndex.put(header.trim(),i);
            i++;
        }
        var metaDataList = new ArrayList<GenericMetaData>();
        for (List<String> strings : excelTableSource.getBody()) {

            var maxIndex = strings.size() - 1;

            GenericMetaData metaData = new GenericMetaData();
            var sjxmIndex  = getIndex(headerIndex,"数据项名（开发自编）","数据项名");

            if(sjxmIndex <= maxIndex){
                metaData.setSjxm(strings.get(sjxmIndex));
            }

            var zwjcIndex  = headerIndex.get("中文简称");
            if(zwjcIndex <= maxIndex){
                metaData.setZwjc(strings.get(zwjcIndex));
            }

            var lxIndex  = headerIndex.get("类型");
            if(lxIndex == null){
                lxIndex = headerIndex.get("类 型");
            }
            if(lxIndex <= maxIndex){
                var lxValue = strings.get(lxIndex);
                metaData.setLx(DataType.valueOf(lxValue));
            }

            var cdIndex = headerIndex.get("长度");
            if(cdIndex <= maxIndex){
                metaData.setCd(Integer.valueOf(strings.get(cdIndex)));
            }

            var ysIndex = headerIndex.get("约束");
            if(ysIndex <= maxIndex){
                metaData.setYs(RestraintType.valueOf(strings.get(ysIndex)));
            }

            var sjgxgzIndex  = headerIndex.get("数据更新规则");
            if(sjgxgzIndex <= maxIndex){
                metaData.setSjgxgz(strings.get(sjgxgzIndex));
            }
            metaDataList.add(metaData);
        }
        tableMetaData.setMetaDataList(metaDataList);
        return tableMetaData;
    }


}
