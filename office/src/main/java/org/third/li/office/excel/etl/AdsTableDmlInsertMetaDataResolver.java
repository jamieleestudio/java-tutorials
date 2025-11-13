package org.third.li.office.excel.etl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdsTableDmlInsertMetaDataResolver implements  TableDmlInsertMetaDataResolver{

    public AdsTableMetaData toTableMetaData(GenericTableSource excelTableSource){

        AdsTableMetaData tableMetaData  = new AdsTableMetaData();

        var tableInfoArr =  excelTableSource.getName().split("-");
        if(tableInfoArr.length < 2){
            tableMetaData.setTableName(tableInfoArr[0]);
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
        var metaDataList = new ArrayList<AdsMetaData>();
        for (List<String> strings : excelTableSource.getBody()) {

            var maxIndex = strings.size() - 1;

            AdsMetaDataImpl metaData = new AdsMetaDataImpl();

            var bhIndex  = headerIndex.get("编号");
            if(bhIndex <= maxIndex){
                metaData.setBh(strings.get(bhIndex));
            }

            var sjxmIndex  = headerIndex.get("数据项名");
            if(sjxmIndex <= maxIndex){
                metaData.setSjxm(strings.get(sjxmIndex));
            }

            var zwjcIndex  = headerIndex.get("中文简称");
            if(zwjcIndex <= maxIndex){
                metaData.setZwjc(strings.get(zwjcIndex));
            }

            var lxIndex  = getIndex(headerIndex,"类型","类 型");
            if(lxIndex <= maxIndex){
                var lxValue = strings.get(lxIndex);
                metaData.setLx(DataType.of(lxValue));
            }

            var cdIndex = headerIndex.get("长度");
            if(cdIndex <= maxIndex){
                metaData.setCd(Integer.valueOf(strings.get(cdIndex)));
            }

            var ysIndex = headerIndex.get("约束");
            if(ysIndex <= maxIndex){
                metaData.setYs(RestraintType.of(strings.get(ysIndex)));
            }

            var zkjIndex = headerIndex.get("值空间");
            if(zkjIndex <= maxIndex){
                metaData.setZkj(strings.get(zkjIndex));
            }

            var jsjlIndex = headerIndex.get("解释/举例");
            if(jsjlIndex <= maxIndex){
                metaData.setJsjl(strings.get(jsjlIndex));
            }

            var yybhIndex = headerIndex.get("引用编号");
            if(yybhIndex <= maxIndex){
                metaData.setYybh(strings.get(yybhIndex));
            }

            var sjgxgzIndex  = headerIndex.get("数据更新规则");
            if(sjgxgzIndex <= maxIndex){
                metaData.setSjgxgz(strings.get(sjgxgzIndex));
            }

            var ztcgfbfzIndex  = headerIndex.get("状态 草稿、发布、废止");
            if(ztcgfbfzIndex <= maxIndex){
                metaData.setZt(strings.get(ztcgfbfzIndex));
            }

            var sfxsIndex  = headerIndex.get("是否显示");
            if(sfxsIndex <= maxIndex){
                metaData.setSfxs(strings.get(sfxsIndex));
            }

            var ysjIdIndex  = headerIndex.get("元数据表ID");
            if(ysjIdIndex <= maxIndex){
                metaData.setYsjbId(strings.get(ysjIdIndex));
            }

            var ggzdKeyIndex  = headerIndex.get("公共字典KEY");
            if(ggzdKeyIndex <= maxIndex){
                metaData.setDictKey(strings.get(ggzdKeyIndex));
            }

            var formatIndex  = headerIndex.get("日期字段在展示时需要格式化显示，如YYYY/MM/DD。更多格式化搜索dayjs format");
            if(formatIndex <= maxIndex){
                metaData.setShowFormat(strings.get(formatIndex));
            }
            metaDataList.add(metaData);
        }
        tableMetaData.setMetaDataList(metaDataList);
        return tableMetaData;
    }

}
