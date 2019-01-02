package com.rulex.bsb.pojo;

import com.google.gson.annotations.SerializedName;

public class WitnessComponent {

    @SerializedName("type")
    private String type;

    /**
     * Data to be included in the input witness (null unless type is "data").
     */
    @SerializedName("value")
    private String value;

    /**
     * The number of signatures required for an input (null unless type is
     * "signature").
     */
    @SerializedName("quorum")
    private int quorum;

    /**
     * The list of keys to sign with (null unless type is "signature").
     */
    @SerializedName("keys")
    private KeyID[] keys;

    /**
     * The list of signatures made with the specified keys (null unless type is
     * "signature").
     */
    @SerializedName("signatures")
    private String[] signatures;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getQuorum() {
        return quorum;
    }

    public void setQuorum(int quorum) {
        this.quorum = quorum;
    }

    public KeyID[] getKeys() {
        return keys;
    }

    public void setKeys(KeyID[] keys) {
        this.keys = keys;
    }

    public String[] getSignatures() {
        return signatures;
    }

    public void setSignatures(String[] signatures) {
        this.signatures = signatures;
    }
}
