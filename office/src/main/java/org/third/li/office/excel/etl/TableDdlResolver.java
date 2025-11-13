package org.third.li.office.excel.etl;

public interface TableDdlResolver extends TableResolver{

    String toString(TableMetaData<MetaData> tableMetaData);

}
