package org.third.li.office.excel.etl;

public interface TableDmlResolver<M extends MetaData> extends TableResolver{

    String toString(TableMetaData<M> tableMetaData);


}
