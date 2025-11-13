package org.third.li.office.excel.etl;

import java.util.ArrayList;
import java.util.List;

public class DwdTableDdlCreateResolver extends AbstractTableDdlCreateResolver{

    public static List<MetaData> META_DATA_LIST = new ArrayList<>();
    static {
        META_DATA_LIST.add(new GenericMetaData("CJSJ","创建时间", DataType.TI,"DEFAULT CURRENT_TIMESTAMP"));
        META_DATA_LIST.add(new GenericMetaData("GXSJ","更新时间",DataType.TI,"DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"));
    }

    @Override
    List<? extends MetaData> getCustomMetaDataList() {
        return META_DATA_LIST;
    }

}
