package org.third.li.office.excel.etl;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

public abstract class AbstractTableDdlCreateResolver implements TableDdlCreateResolver {

    private static final String TABLE_FORMAT = "CREATE TABLE `%s` (\n" +
            "%s"+
            "  PRIMARY KEY (`%s`)\n" +
            ") ENGINE=INNODB DEFAULT CHARSET=%s  COLLATE=%s  COMMENT='%s';";

    @Override
    public String toString(TableMetaData<MetaData> tableMetaData) {
        StringBuilder sqlBody = new StringBuilder();
        String primaryKey  = "";

        List<? extends MetaData> metaDataList = tableMetaData.getMetaDataList();

        for (MetaData metaData : metaDataList) {
            var resolver = new TableDdlFieldCreateResolver(metaData);
            if(resolver.isPrimaryKey()){
                primaryKey = metaData.getSjxm();
            }
            sqlBody.append(new TableDdlFieldCreateResolver(metaData)).append(",\r\n");
        }

        var customMetaDatList = getCustomMetaDataList();
        if(CollectionUtils.isNotEmpty(customMetaDatList))
        for (MetaData metaData : customMetaDatList) {
            sqlBody.append(new TableDdlFieldCreateResolver(metaData)).append(",\r\n");
        }

        return String.format(TABLE_FORMAT,
                tableMetaData.getTableName(),
                sqlBody,
                primaryKey,
                getCharacter(),
                getCollate(),
                tableMetaData.getDescription());
    }

    abstract List<? extends MetaData> getCustomMetaDataList();

    String getCharacter(){
        return "utf8";
    }

    String getCollate(){
        return "utf8_bin";
    }


    class TableDdlFieldCreateResolver {

        private MetaData metaData;

        public TableDdlFieldCreateResolver(MetaData metaData){
            this.metaData = metaData;
        }

        /**
         * field name,type,character,collate ,default value,comment
         * for example:
         * `SYS_PROC_STATUS` VARCHAR(64) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '流程状态'
         */
        public String toString(){
            StringJoiner stringJoiner = new StringJoiner(" ");
            stringJoiner.add(getFieldName());
            stringJoiner.add(getType());

            if(DataType.C == metaData.getLx() ||
               DataType.T == metaData.getLx()){
                stringJoiner.add(getCharacter());
                stringJoiner.add(getCollate());
            }

            stringJoiner.add(getRestraint());
            if(StringUtils.isNotEmpty(metaData.getDefaultValue())){
                stringJoiner.add(metaData.getDefaultValue());
            }
            stringJoiner.add(getComment());
            return stringJoiner.toString();
        }

        public boolean isPrimaryKey(){
            var comment = metaData.getZwjc();
            var updateRuleDescription = metaData.getSjgxgz();
            if(StringUtils.isNotEmpty(comment) &&  comment.contains("主键")){
                return true;
            }
            if(StringUtils.isNotEmpty(updateRuleDescription)){
                if(updateRuleDescription.contains("数据的ID") || updateRuleDescription.contains("数据的实例ID")){
                    return true;
                }
            }
            return false;
        }

        private String getComment(){
            var c = metaData.getZwjc();
            if(StringUtils.isEmpty(c)){
                return "";
            }
            return "COMMENT " + "'"+c.replaceAll("\\R","")+ "'";
        }

        private String getRestraint(){
            if(RestraintType.O == metaData.getYs()){
                return "";
            }else if(RestraintType.M == metaData.getYs()){
                return "NOT NULL";
            }
            return "";
        }

        public String getCollate(){
            var collate = AbstractTableDdlCreateResolver.this.getCollate();
            if(StringUtils.isEmpty(collate)){
                return "";
            }
            return " COLLATE " + collate;
        }

        public String getCharacter(){
            var character = AbstractTableDdlCreateResolver.this.getCharacter();
            if(StringUtils.isEmpty(character)){
                return "";
            }
            return " CHARACTER SET " + character;
        }

        public String getFieldName(){
            return "`"+metaData.getSjxm()+"`";
        }

        public String getType(){
            var type = metaData.getLx();

            var typeString = "";

            if(DataType.C == type){
                typeString =  "VARCHAR";
            }else if(DataType.T == type){
                typeString =   "TEXT";
            }else if(DataType.TI == type){
                typeString =   "TIMESTAMP";
            }else if(DataType.DT == type){
                typeString =   "DATETIME";
            }else if(DataType.TY == type){
                typeString =   "TINYINT";
            }else if(DataType.N == metaData.getLx()){
                var length = Optional.ofNullable(metaData.getCd()).orElse(0);
                if(length < 2){
                    typeString =   "TINYINT";
                }else if(length < 10){
                    typeString =   "INT";
                }else{
                    typeString =   "BIGINT";
                }
            }else {
                throw new RuntimeException(" can not resolve data type.");
            }
            if(metaData.getCd() != null){
                typeString +="("+metaData.getCd()+")";
            }
            return typeString;
        }
    }


}
