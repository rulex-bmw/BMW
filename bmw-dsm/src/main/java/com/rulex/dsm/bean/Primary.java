package com.rulex.dsm.bean;

public class Primary {

    private String name;    // 成员变量名称
    private String type;    // 数据类型
    private String column;  // 列名


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
}
