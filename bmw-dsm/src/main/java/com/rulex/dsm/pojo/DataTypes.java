package com.rulex.dsm.pojo;

public enum DataTypes {

    primeval_int("int"), wrapper_Int("Integer"),
    primeval_long("long"), wrapper_Long("Long"),
    primeval_double("double"), wrapper_Double("Double"),
    primeval_float("float"), wrapper_Float("Float"),
    primeval_string("String"),wrapper_boolean("Boolean"),
    primeval_boolean("boolean"),
    primeval_ByteString("byte[]"),
    primeval_timestamp("timestamp"),
    primeval_datatime("datatime");

    private String name;


    DataTypes(String name) {
        this.name = name;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
