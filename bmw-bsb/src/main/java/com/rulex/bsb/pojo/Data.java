package com.rulex.bsb.pojo;

import com.google.gson.annotations.SerializedName;

public class Data {

    @SerializedName("sign_complete")
    private boolean sign_complete;

    @SerializedName("transaction")
    private Template transaction;

    @SerializedName("tx_id")
    private String tx_id;


    public boolean getSign_complete() {
        return sign_complete;
    }

    public void setSign_complete(boolean sign_complete) {
        this.sign_complete = sign_complete;
    }

    public Template getTransaction() {
        return transaction;
    }

    public void setTransaction(Template transaction) {
        this.transaction = transaction;
    }

    public String getTx_id() {
        return tx_id;
    }

    public void setTx_id(String tx_id) {
        this.tx_id = tx_id;
    }
}
