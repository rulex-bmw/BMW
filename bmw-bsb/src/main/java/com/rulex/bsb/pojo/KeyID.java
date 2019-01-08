package com.rulex.bsb.pojo;

import com.google.gson.annotations.SerializedName;

public class KeyID {

    /**
     * The extended public key associated with the private key used to sign.
     */
    @SerializedName("xpub")
    private String xpub;

    /**
     * The derivation path of the extended public key.
     */
    @SerializedName("derivation_path")
    private String[] derivation_path;

    public String getXpub() {
        return xpub;
    }

    public void setXpub(String xpub) {
        this.xpub = xpub;
    }

    public String[] getDerivation_path() {
        return derivation_path;
    }

    public void setDerivation_path(String[] derivation_path) {
        this.derivation_path = derivation_path;
    }
}
