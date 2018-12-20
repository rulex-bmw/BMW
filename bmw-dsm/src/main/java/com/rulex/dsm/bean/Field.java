package com.rulex.dsm.bean;

public class Field {

    private String name;    // 成员变量名称
    private String type;    // 数据类型
    private String column;  // 列名
    private Boolean isnull; //是否为空
    private Boolean transforable;   // 是否可变
    private Integer maxsize;    // 最大长度
    private Integer minsize;    // 最小长度
    private String maxvalue;    // 最大值
    private String minvalue;    // 最小值
    private Integer length;     // 长度
    private Integer fieldid;    //fieldId
    private boolean isprimaykey;    //是否是主键

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public Boolean getIsnull() {
        return isnull;
    }

    public void setIsnull(Boolean isnull) {
        this.isnull = isnull;
    }

    public Boolean getTransforable() {
        return transforable;
    }

    public void setTransforable(Boolean transforable) {
        this.transforable = transforable;
    }

    public Integer getMaxsize() {
        return maxsize;
    }

    public void setMaxsize(Integer maxsize) {
        this.maxsize = maxsize;
    }

    public Integer getMinsize() {
        return minsize;
    }

    public void setMinsize(Integer minsize) {
        this.minsize = minsize;
    }

    public String getMaxvalue() {
        return maxvalue;
    }

    public void setMaxvalue(String maxvalue) {
        this.maxvalue = maxvalue;
    }

    public String getMinvalue() {
        return minvalue;
    }

    public void setMinvalue(String minvalue) {
        this.minvalue = minvalue;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public Integer getFieldid() {
        return fieldid;
    }

    public void setFieldid(Integer fieldid) {
        this.fieldid = fieldid;
    }

    public boolean isIsprimaykey() {
        return isprimaykey;
    }

    public void setIsprimaykey(boolean isprimaykey) {
        this.isprimaykey = isprimaykey;
    }

    @Override
    public String toString() {
        return "Field{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", column='" + column + '\'' +
                ", isnull=" + isnull +
                ", transforable=" + transforable +
                ", maxsize=" + maxsize +
                ", minsize=" + minsize +
                ", maxvalue='" + maxvalue + '\'' +
                ", minvalue='" + minvalue + '\'' +
                ", length=" + length +
                ", fieldid=" + fieldid +
                ", isprimaykey=" + isprimaykey +
                '}';
    }
}
