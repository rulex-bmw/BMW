package com.rulex.bsb.pojo;

import com.google.gson.annotations.SerializedName;

public class Transaction {

    @SerializedName("password")
    private String password;
    @SerializedName("transaction")
    private Template transaction;



    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Template getTransaction() {
        return transaction;
    }

    public void setTransaction(Template transaction) {
        this.transaction = transaction;
    }


}
