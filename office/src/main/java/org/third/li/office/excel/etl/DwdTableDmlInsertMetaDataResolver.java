package org.third.li.office.excel.etl;

import java.util.*;

public class DwdTableDmlInsertMetaDataResolver implements  TableDmlInsertMetaDataResolver{



    public DwdTableMetaData toTableMetaData(GenericTableSource excelTableSource){

        DwdTableMetaData tableMetaData  = new DwdTableMetaData();

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
        var metaDataList = new ArrayList<DwdMetaData>();
        for (List<String> strings : excelTableSource.getBody()) {

            var maxIndex = strings.size() - 1;

            DwdMetaDataImpl metaData = new DwdMetaDataImpl();

//            var bhIndex  = getIndex(headerIndex,"编号（开发自编）","编号");
//            if(bhIndex <= maxIndex){
//                metaData.setBh(strings.get(bhIndex));
//            }

            var sjxmIndex  = getIndex(headerIndex,"数据项名（开发自编）","数据项名");
            if(sjxmIndex <= maxIndex){
                metaData.setSjxm(strings.get(sjxmIndex));
            }

            var zwjcIndex  = headerIndex.get("中文简称");
            if(zwjcIndex <= maxIndex){
                metaData.setZwjc(strings.get(zwjcIndex));
            }

            var sjgxgzIndex  = headerIndex.get("数据更新规则");
            if(sjgxgzIndex <= maxIndex){
                metaData.setSjgxgz(strings.get(sjgxgzIndex));
            }

            var lxIndex  = headerIndex.get("类型");
            if(lxIndex == null){
                lxIndex = headerIndex.get("类 型");
            }
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

            var syccIndex = headerIndex.get("适用层次");
            if(syccIndex <= maxIndex){
                metaData.setSycc(strings.get(syccIndex));
            }

            var zxbzIndex = headerIndex.get("遵循标准");
            if(zxbzIndex <= maxIndex){
                metaData.setZxbz(strings.get(zxbzIndex));
            }

            var ckdbzwjIndex = headerIndex.get("参考的标准文件名称");
            if(ckdbzwjIndex <= maxIndex){
                metaData.setCkbzwj(strings.get(ckdbzwjIndex));
            }

            var bzssdzdzjIndex = headerIndex.get("标准所属的字段子集");
            if(bzssdzdzjIndex <= maxIndex){
                metaData.setCkbzzdj(strings.get(bzssdzdzjIndex));
            }

            var ssywIndex = headerIndex.get("所属业务");
            if(ssywIndex <= maxIndex){
                metaData.setSsyw(strings.get(ssywIndex));
            }

            var ssywflIndex = headerIndex.get("所属业务分类");
            if(ssywflIndex <= maxIndex){
                metaData.setSsywfl(strings.get(ssywflIndex));
            }

            var zdmsIndex = headerIndex.get("字段描述");
            if(zdmsIndex <= maxIndex){
                metaData.setZdms(strings.get(zdmsIndex));
            }

            var ssysjbIdIndex = headerIndex.get("所属元数据表ID");
            if(ssysjbIdIndex <= maxIndex){
                metaData.setYsjbId(strings.get(ssysjbIdIndex));
            }

            var lyIndex = headerIndex.get("来源");
            if(lyIndex <= maxIndex){
                metaData.setLy(strings.get(lyIndex));
            }

            var ztcgfbfzIndex  = headerIndex.get("草稿、发布、废止");
            if(ztcgfbfzIndex <= maxIndex){
                metaData.setZt(strings.get(ztcgfbfzIndex));
            }

            var sfxsIndex  = headerIndex.get("是否显示 0：不显示 1：显示");
            if(sfxsIndex <= maxIndex){
                metaData.setSfxs(strings.get(sfxsIndex));
            }


            var versionIndex  = headerIndex.get("迭代版本号");
            if(versionIndex <= maxIndex){
                metaData.setVersion(strings.get(versionIndex));
            }

            var pxIndex  = headerIndex.get("排序");
            if(pxIndex <= maxIndex){
                metaData.setPx(strings.get(pxIndex));
            }
            metaDataList.add(metaData);
        }
        tableMetaData.setMetaDataList(metaDataList);
        return tableMetaData;
    }

}
