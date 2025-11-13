package org.third.li.office.excel.etl;

import java.util.List;

public interface TableMetaData<T extends MetaData> {

    String getTableName();

    String getDescription();

    List<T> getMetaDataList();

    void setMetaDataList(List<T> metaDataList);

}
