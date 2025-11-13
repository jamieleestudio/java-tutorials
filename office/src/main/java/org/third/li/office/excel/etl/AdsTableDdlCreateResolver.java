package org.third.li.office.excel.etl;

import java.util.ArrayList;
import java.util.List;

public class AdsTableDdlCreateResolver extends AbstractTableDdlCreateResolver{

    public static List<MetaData> META_DATA_LIST = new ArrayList<>();
    static {
        META_DATA_LIST.add(new GenericMetaData("CJSJ","创建时间", DataType.TI,"DEFAULT CURRENT_TIMESTAMP"));
        META_DATA_LIST.add(new GenericMetaData("GXSJ","更新时间",DataType.TI,"DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"));
        META_DATA_LIST.add(new GenericMetaData("SBSJ","上报时间",DataType.DT));
        META_DATA_LIST.add(new GenericMetaData("SJCJRYSJ","数据采集冗余时间（用于查询比较）",DataType.DT));
        META_DATA_LIST.add(new GenericMetaData("SBZT","上报状态 -1：待上报 0:已推送 1:上报成功 2:待重新上报 3：上报失败",DataType.TY,2));
        META_DATA_LIST.add(new GenericMetaData("SC","删除",DataType.TY,1));
        META_DATA_LIST.add(new GenericMetaData("SBXX","失败信息",DataType.C,5000));
        META_DATA_LIST.add(new GenericMetaData("UPLOAD_RECORD_ID","上报记录ID",DataType.C,50));
    }

    @Override
    List<? extends MetaData> getCustomMetaDataList() {
        return META_DATA_LIST;
    }

}
