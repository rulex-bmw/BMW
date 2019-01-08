package com.rulex.bsb.pojo;

import com.google.gson.annotations.SerializedName;

public class BuildReturn {

    @SerializedName("status")
    private String status;
    @SerializedName("data")
    private Template data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Template getData() {
        return data;
    }

    public void setData(Template data) {
        this.data = data;
    }
}
