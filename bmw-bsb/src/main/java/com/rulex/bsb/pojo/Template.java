package com.rulex.bsb.pojo;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Template {

    @SerializedName("raw_transaction")
    private String raw_transaction;
    @SerializedName("signing_instructions")
    private List<SigningInstruction> signing_instructions;
    @SerializedName("local")
    private boolean local;
    @SerializedName("allow_additional_actions")
    private boolean allow_additional_actions;

    public String getRaw_transaction() {
        return raw_transaction;
    }

    public void setRaw_transaction(String raw_transaction) {
        this.raw_transaction = raw_transaction;
    }

    public boolean isLocal() {
        return local;
    }

    public void setLocal(boolean local) {
        this.local = local;
    }

    public boolean isAllow_additional_actions() {
        return allow_additional_actions;
    }

    public void setAllow_additional_actions(boolean allow_additional_actions) {
        this.allow_additional_actions = allow_additional_actions;
    }

    public List<SigningInstruction> getSigning_instructions() {
        return signing_instructions;
    }

    public void setSigning_instructions(List<SigningInstruction> signing_instructions) {
        this.signing_instructions = signing_instructions;
    }
}
