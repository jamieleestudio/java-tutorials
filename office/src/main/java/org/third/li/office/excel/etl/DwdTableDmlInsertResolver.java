package org.third.li.office.excel.etl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Function;

public class DwdTableDmlInsertResolver implements TableDmlInsertResolver<DwdMetaData>{

    static class DwdFunctionMetaDataColumn extends FunctionMetaDataColumn<DwdMetaData>{
        public DwdFunctionMetaDataColumn(MetaDataColumn metaDataColumn, Function<DwdMetaData,? extends Serializable> function) {
            super(metaDataColumn, function);
        }
    }

    private static final List<DwdFunctionMetaDataColumn> MEDA_DATA_COLUMN = new ArrayList<>();
    static {

//        MEDA_DATA_COLUMN.add(new DwdFunctionMetaDataColumn(new MetaDataColumn("编号（开发自编）","BH"),DwdMetaData::getBh));
        MEDA_DATA_COLUMN.add(new DwdFunctionMetaDataColumn(new MetaDataColumn("数据项名（开发自编）","SJXM"),DwdMetaData::getSjxm));
        MEDA_DATA_COLUMN.add(new DwdFunctionMetaDataColumn(new MetaDataColumn("中文简称","ZWJC"),DwdMetaData::getZwjc));
        MEDA_DATA_COLUMN.add(new DwdFunctionMetaDataColumn(new MetaDataColumn("数据更新规则","SJGXGZ"),DwdMetaData::getSjgxgz));
        MEDA_DATA_COLUMN.add(new DwdFunctionMetaDataColumn(new MetaDataColumn("类型","LX"),DwdMetaData::getLx));
        MEDA_DATA_COLUMN.add(new DwdFunctionMetaDataColumn(new MetaDataColumn("长度","CD"),DwdMetaData::getCd));
        MEDA_DATA_COLUMN.add(new DwdFunctionMetaDataColumn(new MetaDataColumn("约束","YS"),DwdMetaData::getYs));
        MEDA_DATA_COLUMN.add(new DwdFunctionMetaDataColumn(new MetaDataColumn("值空间","ZKJ"),DwdMetaData::getZkj));
        MEDA_DATA_COLUMN.add(new DwdFunctionMetaDataColumn(new MetaDataColumn("解释/举例","JSJL"),DwdMetaData::getJsjl));
        MEDA_DATA_COLUMN.add(new DwdFunctionMetaDataColumn(new MetaDataColumn("引用编号","YYBH"),DwdMetaData::getYybh));
        MEDA_DATA_COLUMN.add(new DwdFunctionMetaDataColumn(new MetaDataColumn("适用层次","SYCC"),DwdMetaData::getSycc));
        MEDA_DATA_COLUMN.add(new DwdFunctionMetaDataColumn(new MetaDataColumn("遵循标准","ZXBZ"),DwdMetaData::getZxbz));
        MEDA_DATA_COLUMN.add(new DwdFunctionMetaDataColumn(new MetaDataColumn("参考的标准文件名称","CKBZWJ"),DwdMetaData::getCkbzwj));
        MEDA_DATA_COLUMN.add(new DwdFunctionMetaDataColumn(new MetaDataColumn("标准所属的字段子集","CKBZZDJ"),DwdMetaData::getCkbzzdj));
        MEDA_DATA_COLUMN.add(new DwdFunctionMetaDataColumn(new MetaDataColumn("所属业务","SSYW"),DwdMetaData::getSsyw));
        MEDA_DATA_COLUMN.add(new DwdFunctionMetaDataColumn(new MetaDataColumn("所属业务分类","SSYWFL"),DwdMetaData::getSsywfl));
        MEDA_DATA_COLUMN.add(new DwdFunctionMetaDataColumn(new MetaDataColumn("字段描述","ZDMS"),DwdMetaData::getZdms));
        MEDA_DATA_COLUMN.add(new DwdFunctionMetaDataColumn(new MetaDataColumn("草稿、发布、废止","ZT"),DwdMetaData::getZt));
        MEDA_DATA_COLUMN.add(new DwdFunctionMetaDataColumn(new MetaDataColumn("所属元数据表ID","YSJB_ID"),DwdMetaData::getYsjbId));
        MEDA_DATA_COLUMN.add(new DwdFunctionMetaDataColumn(new MetaDataColumn("来源","LY"),DwdMetaData::getLy));
        MEDA_DATA_COLUMN.add(new DwdFunctionMetaDataColumn(new MetaDataColumn("是否显示 0：不显示 1：显示","SFXS"),DwdMetaData::getSfxs));
        MEDA_DATA_COLUMN.add(new DwdFunctionMetaDataColumn(new MetaDataColumn("迭代版本号","VERSION"),DwdMetaData::getVersion));
        MEDA_DATA_COLUMN.add(new DwdFunctionMetaDataColumn(new MetaDataColumn("排序","PX"),DwdMetaData::getPx));
    }
    private static final String INSERT_SQL_FORMAT =  "INSERT INTO ZT_YSJBSX (%s) VALUES (%s); ";

    @Override
    public String toString(TableMetaData<DwdMetaData> tableMetaData) {

        StringJoiner stringJoiner = new StringJoiner("\r\n");

        for (DwdMetaData metaData : tableMetaData.getMetaDataList()) {

            var columnJoiner = new StringJoiner(",");
            var valuesJoiner = new StringJoiner(",");

            for (DwdFunctionMetaDataColumn adsFunctionMetaDataColumn : MEDA_DATA_COLUMN) {
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
