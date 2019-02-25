package com.instwall.base.data;

import java.util.List;

/**
 * Created by phcy on 10/13/17.
 */

public class RequestResult {
    public List<DetectFace> mDetectFaceList;
    public DetectFace.VipAuth mVipAuth;
    public String errorMessage;
    public DetectFace mTrackFace;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RequestResult that = (RequestResult) o;

        if (mDetectFaceList != null ? !mDetectFaceList.equals(that.mDetectFaceList) : that.mDetectFaceList != null)
            return false;
        if (mVipAuth != null ? !mVipAuth.equals(that.mVipAuth) : that.mVipAuth != null)
            return false;
        if (errorMessage != null ? !errorMessage.equals(that.errorMessage) : that.errorMessage != null)
            return false;
        return mTrackFace != null ? mTrackFace.equals(that.mTrackFace) : that.mTrackFace == null;
    }

    @Override
    public int hashCode() {
        int result = mDetectFaceList != null ? mDetectFaceList.hashCode() : 0;
        result = 31 * result + (mVipAuth != null ? mVipAuth.hashCode() : 0);
        result = 31 * result + (errorMessage != null ? errorMessage.hashCode() : 0);
        result = 31 * result + (mTrackFace != null ? mTrackFace.hashCode() : 0);
        return result;
    }
}
