package org.third.li.office.excel.etl;

import org.apache.commons.lang3.StringUtils;

public class GenericMetaData implements MetaData {

    private String sjxm;

    private String zwjc;

    private DataType lx;

    private Integer cd;

    private RestraintType ys;

    private String sjgxgz;

    private String defaultValue;

    public GenericMetaData() {
    }

    public GenericMetaData(String dataItemName, String chineseName, DataType metaDataType) {
        this.sjxm = dataItemName;
        this.zwjc = chineseName;
        this.lx = metaDataType;
    }


    public GenericMetaData(String dataItemName, String chineseName, DataType metaDataType, String defaultValue) {
        this.sjxm = dataItemName;
        this.zwjc = chineseName;
        this.lx = metaDataType;
        this.defaultValue = defaultValue;
    }

    public GenericMetaData(String dataItemName, String chineseName, DataType metaDataType, RestraintType restraintType) {
        this.sjxm = dataItemName;
        this.zwjc = chineseName;
        this.lx = metaDataType;
        this.ys = restraintType;
    }

    public GenericMetaData(String dataItemName, String chineseName, DataType metaDataType, Integer length) {
        this.sjxm = dataItemName;
        this.zwjc = chineseName;
        this.lx = metaDataType;
        this.cd = length;
    }

    public GenericMetaData(String dataItemName, String chineseName, DataType metaDataType, Integer length, RestraintType restraintType) {
        this.sjxm = dataItemName;
        this.zwjc = chineseName;
        this.lx = metaDataType;
        this.cd = length;
        this.ys = restraintType;
    }

    @Override
    public String getSjxm() {
        if(StringUtils.isEmpty(sjxm)){
            this.setSjxm(PinyinUtils.getFirstLetter(zwjc));
        }
        return sjxm;
    }

    public void setSjxm(String sjxm) {
        this.sjxm = sjxm;
    }

    @Override
    public String getZwjc() {
        return zwjc;
    }

    public void setZwjc(String zwjc) {
        this.zwjc = zwjc;
    }

    @Override
    public DataType getLx() {
        return lx;
    }

    public void setLx(DataType lx) {
        this.lx = lx;
    }

    @Override
    public Integer getCd() {
        return cd;
    }

    public void setCd(Integer cd) {
        this.cd = cd;
    }

    @Override
    public RestraintType getYs() {
        return ys;
    }

    public void setYs(RestraintType ys) {
        this.ys = ys;
    }

    public String getSjgxgz() {
        return sjgxgz;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }


    public void setSjgxgz(String sjgxgz) {
        this.sjgxgz = sjgxgz;
    }
}
