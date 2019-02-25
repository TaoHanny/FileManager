package com.install.arc.data;

import android.graphics.Rect;

public class ArcFace {
    public int getTrackId() {
        return mTrackId;
    }

    public void setTrackId(int mTrackId) {
        this.mTrackId = mTrackId;
    }

    public long getTrackTime() {
        return mTrackTime;
    }

    public void setTrackTime(long mTrackTime) {
        this.mTrackTime = mTrackTime;
    }

    public Rect getRect() {
        return mRect;
    }

    public void setRect(Rect mRect) {
        this.mRect = mRect;
    }

    private int mTrackId;
    private byte[] mFeature;
    private long mTrackTime;
    private Rect mRect;


    public ArcFace(ArcFace obj) {
        if (obj == null) {
            this.mFeature = new byte[1032];
        } else {
            this.mFeature = (byte[])obj.getFeatureData().clone();
        }

    }

    public ArcFace() {
        this.mFeature = new byte[1032];
    }


    public ArcFace(byte[] data) {
        this.mFeature = data;
    }

    public byte[] getFeatureData() {
        return this.mFeature;
    }

    public void setFeatureData(byte[] data) {
        this.mFeature = data;
    }

    public ArcFace clone() {
        return new ArcFace(this);
    }




    }