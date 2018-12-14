package com.rulex.dsm.bean;

import java.util.List;

public class Source {

    private String name;    // pojo名称
    private String pojo;    // pojo具体路径
    private String table;   // 表名
    private Boolean groupable;  // 是否可合并
    private List<Field> fields; // 成员变量

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPojo() {
        return pojo;
    }

    public void setPojo(String pojo) {
        this.pojo = pojo;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public Boolean getGroupable() {
        return groupable;
    }

    public void setGroupable(Boolean groupable) {
        this.groupable = groupable;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    @Override
    public String toString() {
        return "Source{" +
                "name='" + name + '\'' +
                ", pojo='" + pojo + '\'' +
                ", table='" + table + '\'' +
                ", groupable=" + groupable +
                ", fields=" + fields +
                '}';
    }
}
