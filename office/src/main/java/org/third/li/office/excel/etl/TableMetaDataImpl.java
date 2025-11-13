package org.third.li.office.excel.etl;

import java.util.List;

public class TableMetaDataImpl<T extends MetaData> implements TableMetaData<T>{

    private String tableName;

    private String description;

    private List<T> metaDataList;


    @Override
    public String getTableName() {
        return tableName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public List<T> getMetaDataList() {
        return metaDataList;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setMetaDataList(List<T> metaDataList) {
        this.metaDataList = metaDataList;
    }
}
