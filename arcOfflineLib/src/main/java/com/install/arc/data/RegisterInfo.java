package com.install.arc.data;

public class RegisterInfo {
    private byte[] featureData;
    private String name;

    public RegisterInfo(byte[] faceFeature, String name) {
        this.featureData = faceFeature;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getFeatureData() {
        return featureData;
    }

    public void setFeatureData(byte[] featureData) {
        this.featureData = featureData;
    }
}
