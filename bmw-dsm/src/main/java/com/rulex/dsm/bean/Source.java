package com.rulex.dsm.bean;

import java.util.List;

public class Source {

    private String name;
    private String table;
    private Boolean groupable;
    private List<Field> fields;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
                ", table='" + table + '\'' +
                ", groupable=" + groupable +
                ", fields=" + fields +
                '}';
    }
}
