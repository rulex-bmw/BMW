package com.rulex.dsm.bean;

public class Field {

    private String name;
    private String type;
    private String column;
    private Boolean isnull;
    private Boolean transforable;
    private Integer maxsize;
    private Integer minsize;
    private String maxvalue;
    private String minvalue;
    private Integer length;

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
                '}';
    }
}
