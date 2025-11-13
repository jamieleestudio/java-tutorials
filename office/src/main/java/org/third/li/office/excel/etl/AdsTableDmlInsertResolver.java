package org.third.li.office.excel.etl;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;

public class AdsTableDmlInsertResolver implements TableDmlInsertResolver<AdsMetaData>{

    static class AdsFunctionMetaDataColumn extends FunctionMetaDataColumn<AdsMetaData>{
        public AdsFunctionMetaDataColumn(MetaDataColumn metaDataColumn, Function<AdsMetaData,? extends Serializable> function) {
            super(metaDataColumn, function);
        }
    }
    private static  final  List<AdsFunctionMetaDataColumn> MEDA_DATA_COLUMN = new ArrayList<>();
    static {
        MEDA_DATA_COLUMN.add(new AdsFunctionMetaDataColumn(new MetaDataColumn("编号", "BH"), AdsMetaData::getBh));
        MEDA_DATA_COLUMN.add(new AdsFunctionMetaDataColumn(new MetaDataColumn("数据项名","SJXM"),AdsMetaData::getSjxm));
        MEDA_DATA_COLUMN.add(new AdsFunctionMetaDataColumn(new MetaDataColumn("中文简称","ZWJC"),AdsMetaData::getZwjc));
        MEDA_DATA_COLUMN.add(new AdsFunctionMetaDataColumn(new MetaDataColumn("类 型","LX"),AdsMetaData::getLx));
        MEDA_DATA_COLUMN.add(new AdsFunctionMetaDataColumn(new MetaDataColumn("长度","CD"),AdsMetaData::getCd));
        MEDA_DATA_COLUMN.add(new AdsFunctionMetaDataColumn(new MetaDataColumn("约束","YS"),AdsMetaData::getYs));
        MEDA_DATA_COLUMN.add(new AdsFunctionMetaDataColumn(new MetaDataColumn("值空间","ZKJ"),AdsMetaData::getZkj));
        MEDA_DATA_COLUMN.add(new AdsFunctionMetaDataColumn(new MetaDataColumn("解释/举例","JSJL"),AdsMetaData::getJsjl));
        MEDA_DATA_COLUMN.add(new AdsFunctionMetaDataColumn(new MetaDataColumn("引用编号","YYBH"),AdsMetaData::getYybh));
        MEDA_DATA_COLUMN.add(new AdsFunctionMetaDataColumn(new MetaDataColumn("数据更新规则","SJGXGZ"),AdsMetaData::getSjgxgz));
        MEDA_DATA_COLUMN.add(new AdsFunctionMetaDataColumn(new MetaDataColumn("状态 草稿、发布、废止","ZT"),AdsMetaData::getZt));
        MEDA_DATA_COLUMN.add(new AdsFunctionMetaDataColumn(new MetaDataColumn("是否显示","SFXS"),AdsMetaData::getSfxs));
        MEDA_DATA_COLUMN.add(new AdsFunctionMetaDataColumn(new MetaDataColumn("元数据表ID","YSJB_ID"),AdsMetaData::getYsjbId));
        MEDA_DATA_COLUMN.add(new AdsFunctionMetaDataColumn(new MetaDataColumn("公共字典KEY","DICT_KEY"),AdsMetaData::getDictKey));
        MEDA_DATA_COLUMN.add(new AdsFunctionMetaDataColumn(new MetaDataColumn("日期字段在展示时需要格式化显示，如YYYY/MM/DD。更多格式化搜索dayjs format","SHOW_FORMAT"),AdsMetaData::getShowFormat));
    }

    private static final String INSERT_SQL_FORMAT =  "INSERT INTO YSJBSX (%s) VALUES (%s); ";


    @Override
    public String toString(TableMetaData<AdsMetaData> tableMetaData) {

        StringJoiner stringJoiner = new StringJoiner("\r\n");

        for (AdsMetaData metaData : tableMetaData.getMetaDataList()) {

            var columnJoiner = new StringJoiner(",");
            var valuesJoiner = new StringJoiner(",");

            for (AdsFunctionMetaDataColumn adsFunctionMetaDataColumn : MEDA_DATA_COLUMN) {
                var column = adsFunctionMetaDataColumn.getMetaDataColumn();
                columnJoiner.add(column.getDbColumn());
                var value = adsFunctionMetaDataColumn.getFunction().apply(metaData);
                if(value == null){
                    valuesJoiner.add(null);
                }else{
                    var v = String.valueOf(value).replaceAll("\\R","");
                    valuesJoiner.add("'"+v+"'");
                }
            }
            stringJoiner.add(String.format(INSERT_SQL_FORMAT,columnJoiner,valuesJoiner));
        }
        return stringJoiner.toString();
    }


}
