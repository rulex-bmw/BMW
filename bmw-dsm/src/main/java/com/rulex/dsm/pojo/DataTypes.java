package com.rulex.dsm.pojo;

public enum DataTypes {

    primeval_byte("byte"), primeval_short("short"), primeval_int("int"), primeval_long("long"),
    wrapper_Byte("Byte"), wrapper_Short("Short"), wrapper_Int("Integer"), wrapper_Long("Long"),
    primeval_float("float"), primeval_double("double"),
    wrapper_Float("Float"), wrapper_Double("Double"),
    primeval_string("String");

    private String name;


    private DataTypes(String name) {
        this.name = name;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
