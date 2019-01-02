package com.rulex.bsb.pojo;

import com.google.gson.annotations.SerializedName;

public class SigningInstruction {

    @SerializedName("position")
    private int position;

    @SerializedName("witness_components")
    private WitnessComponent[] witnessComponents;

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public WitnessComponent[] getWitnessComponents() {
        return witnessComponents;
    }

    public void setWitnessComponents(WitnessComponent[] witnessComponents) {
        this.witnessComponents = witnessComponents;
    }
}
