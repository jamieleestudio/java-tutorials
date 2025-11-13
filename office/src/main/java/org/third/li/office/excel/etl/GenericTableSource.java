package org.third.li.office.excel.etl;

public interface GenericTableSource extends TableSource{

    void refresh();

    default int getHeaderStartNumber(){
        return 1;
    }

    void setHeaderStartNumber(int headerStartNumber);

    default int getBodyStartNumber(){
        return getHeaderStartNumber()+1;
    }

}
