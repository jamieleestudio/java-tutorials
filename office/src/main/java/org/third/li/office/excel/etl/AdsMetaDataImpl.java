package org.third.li.office.excel.etl;

public class AdsMetaDataImpl extends GenericMetaData implements AdsMetaData{

    private String bh;

    private String zkj;

    private String jsjl;

    private String yybh;

    private String zt;

    private String sfxs;

    private String ysjbId;

    private String dictKey;

    private String showFormat;

    @Override
    public String getBh() {
        return bh;
    }

    public void setBh(String bh) {
        this.bh = bh;
    }

    @Override
    public String getZkj() {
        return zkj;
    }

    public void setZkj(String zkj) {
        this.zkj = zkj;
    }

    @Override
    public String getJsjl() {
        return jsjl;
    }

    public void setJsjl(String jsjl) {
        this.jsjl = jsjl;
    }

    @Override
    public String getYybh() {
        return yybh;
    }

    public void setYybh(String yybh) {
        this.yybh = yybh;
    }

    @Override
    public String getZt() {
        return zt;
    }

    public void setZt(String zt) {
        this.zt = zt;
    }

    @Override
    public String getSfxs() {
        return sfxs;
    }

    public void setSfxs(String sfxs) {
        this.sfxs = sfxs;
    }

    @Override
    public String getYsjbId() {
        return ysjbId;
    }

    public void setYsjbId(String ysjbId) {
        this.ysjbId = ysjbId;
    }

    @Override
    public String getDictKey() {
        return dictKey;
    }

    public void setDictKey(String dictKey) {
        this.dictKey = dictKey;
    }

    @Override
    public String getShowFormat() {
        return showFormat;
    }

    public void setShowFormat(String showFormat) {
        this.showFormat = showFormat;
    }
}
