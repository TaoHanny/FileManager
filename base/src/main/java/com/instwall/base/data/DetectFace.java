package com.instwall.base.data;

import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.StringDef;

import com.instwall.base.FaceConfig;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Created by phcy on 17-8-24.
 */

public class DetectFace {
    public int mTackID;
    @FaceConfig.EngineModule
    public int mEngineMode;
    public Rect mRect;
    public Attribute mAttribute;
    public VipAuth mVipAuth;
    public StObj mStObj;
    public GoogleObj mGoogleObj;
    public KangShiObj mKangShiObj;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DetectFace that = (DetectFace) o;
        return mTackID == that.mTackID &&
                mEngineMode == that.mEngineMode &&
                Objects.equals(mRect, that.mRect) &&
                Objects.equals(mAttribute, that.mAttribute) &&
                Objects.equals(mVipAuth, that.mVipAuth) &&
                Objects.equals(mStObj, that.mStObj) &&
                Objects.equals(mGoogleObj, that.mGoogleObj) &&
                Objects.equals(mKangShiObj, that.mKangShiObj);
    }

    @Override
    public int hashCode() {

        return Objects.hash(mTackID, mEngineMode, mRect, mAttribute, mVipAuth, mStObj, mGoogleObj, mKangShiObj);
    }



    public static final String EMOTION_ANGER = "愤怒";
    public static final String EMOTION_CALM = "平静";
    public static final String EMOTION_CONFUSED = "困惑";
    public static final String EMOTION_DISGUST = "厌恶";
    public static final String EMOTION_HAPPY = "高兴";
    public static final String EMOTION_SAD = "悲伤";
    public static final String EMOTION_FRIGHTENED = "惊恐";
    public static final String EMOTION_SURPRISED = "诧异";
    public static final String EMOTION_SQUINT = "斜视";
    public static final String EMOTION_SCREAMING = "尖叫";

    @StringDef({EMOTION_ANGER, EMOTION_CALM, EMOTION_CONFUSED,
            EMOTION_DISGUST, EMOTION_HAPPY, EMOTION_SAD, EMOTION_FRIGHTENED,
            EMOTION_SURPRISED, EMOTION_SQUINT, EMOTION_SCREAMING})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Emotion {
    }

    public static class VipAuth {
        public final String authID;
        public final String token;
        public final double confidence;

        public VipAuth(String authID, String token, double confidence) {
            this.authID = authID;
            this.token = token;
            this.confidence = confidence;
        }
    }

    public static class Attribute {
        public final String gender;
        public final int beauty;//颜值
        public final int age;
        public final boolean glasses;
        public final boolean mask;// 口罩
        public final boolean eyeOpen;
        public final boolean beard;// 胡须
        public final boolean smile;
        public final int smileValue;
        public final boolean mouthOpen;
        @Emotion
        public final String emotionType;

        public Attribute(String gender, int beauty, int age, boolean glasses,
                         boolean mask, boolean eyeOpen, boolean beard, boolean smile, int smileValue, boolean mouthOpen, String emotionType) {
            this.gender = gender;
            this.beauty = beauty;
            this.age = age;
            this.glasses = glasses;
            this.mask = mask;
            this.eyeOpen = eyeOpen;
            this.beard = beard;
            this.smile = smile;
            this.smileValue = smileValue;
            this.mouthOpen = mouthOpen;
            this.emotionType = emotionType;
        }

        @Override
        public String toString() {
            return "Attribute{" +
                    "gender='" + gender + '\'' +
                    ", beauty=" + beauty +
                    ", age=" + age +
                    ", glasses=" + glasses +
                    ", mask=" + mask +
                    ", eyeOpen=" + eyeOpen +
                    ", beard=" + beard +
                    ", smile=" + smile +
                    ", smileValue=" + smileValue +
                    ", mouthOpen=" + mouthOpen +
                    ", emotionType='" + emotionType + '\'' +
                    '}';
        }
    }



    ///////////////////////////////////////////
    // for St
    public static class StObj {
        public final PointF[] mFacePoints;
        public final float mScore;
        public final float mYaw;
        public final float mPitch;
        public final float mRoll;
        public final float mEyeDist;

        public StObj(PointF[] mFacePoints, float mScore, float mYaw, float mPitch, float mRoll, float mEyeDist) {
            this.mFacePoints = mFacePoints;
            this.mScore = mScore;
            this.mYaw = mYaw;
            this.mPitch = mPitch;
            this.mRoll = mRoll;
            this.mEyeDist = mEyeDist;
        }

        @Override
        public String toString() {
            return "DetectFace{" +
                    ", mFacePoints=" + Arrays.toString(mFacePoints) +
                    ", mScore=" + mScore +
                    ", mYaw=" + mYaw +
                    ", mPitch=" + mPitch +
                    ", mRoll=" + mRoll +
                    ", mEyeDist=" + mEyeDist +
                    '}';
        }

    }

    public static class PointF {
        public float x;
        public float y;


        public PointF(float var1, float var2) {
            this.x = var1;
            this.y = var2;
        }


        @Override
        public String toString() {
            return "PointF{" +
                    "x=" + x +
                    ", y=" + y +
                    '}';
        }
    }

    ////////////////////////////////////////////////////
    //Google Obj
    public static class GoogleObj implements Parcelable {
        public android.graphics.PointF mCenterPoint;
        public List<Landmark> mLandmarks;
        public float mIsLeftEyeOpenProbability;
        public float mIsRightEyeOpenProbability;
        public float mIsSmilingProbability;

        public GoogleObj(android.graphics.PointF point, List<Landmark> mLandmarks, float mIsLeftEyeOpenProbability,
                         float mIsRightEyeOpenProbability, float mIsSmilingProbability) {
            this.mCenterPoint = point;
            this.mLandmarks = mLandmarks;
            this.mIsLeftEyeOpenProbability = mIsLeftEyeOpenProbability;
            this.mIsRightEyeOpenProbability = mIsRightEyeOpenProbability;
            this.mIsSmilingProbability = mIsSmilingProbability;

        }

        @Override
        public String toString() {
            return "GoogleObj{" +
                    "mCenterPoint=" + mCenterPoint +
                    ", mLandmarks=" + mLandmarks +
                    ", mIsLeftEyeOpenProbability=" + mIsLeftEyeOpenProbability +
                    ", mIsRightEyeOpenProbability=" + mIsRightEyeOpenProbability +
                    ", mIsSmilingProbability=" + mIsSmilingProbability +
                    '}';
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(this.mCenterPoint, flags);
            dest.writeList(this.mLandmarks);
            dest.writeFloat(this.mIsLeftEyeOpenProbability);
            dest.writeFloat(this.mIsRightEyeOpenProbability);
            dest.writeFloat(this.mIsSmilingProbability);
        }

        protected GoogleObj(Parcel in) {
            this.mCenterPoint = in.readParcelable(android.graphics.PointF.class.getClassLoader());
            this.mLandmarks = new ArrayList<Landmark>();
            in.readList(this.mLandmarks, Landmark.class.getClassLoader());
            this.mIsLeftEyeOpenProbability = in.readFloat();
            this.mIsRightEyeOpenProbability = in.readFloat();
            this.mIsSmilingProbability = in.readFloat();
        }

        public static final Parcelable.Creator<GoogleObj> CREATOR = new Parcelable.Creator<GoogleObj>() {
            @Override
            public GoogleObj createFromParcel(Parcel source) {
                return new GoogleObj(source);
            }

            @Override
            public GoogleObj[] newArray(int size) {
                return new GoogleObj[size];
            }
        };
    }

    public static class Landmark {
        public static final int BOTTOM_MOUTH = 0;
        public static final int LEFT_CHEEK = 1;
        public static final int LEFT_EAR_TIP = 2;
        public static final int LEFT_EAR = 3;
        public static final int LEFT_EYE = 4;
        public static final int LEFT_MOUTH = 5;
        public static final int NOSE_BASE = 6;
        public static final int RIGHT_CHEEK = 7;
        public static final int RIGHT_EAR_TIP = 8;
        public static final int RIGHT_EAR = 9;
        public static final int RIGHT_EYE = 10;
        public static final int RIGHT_MOUTH = 11;
        private final android.graphics.PointF mPosition;
        private final int mType;

        @IntDef({BOTTOM_MOUTH, LEFT_CHEEK, LEFT_EAR_TIP, LEFT_EAR, LEFT_EYE, LEFT_MOUTH, NOSE_BASE,
                RIGHT_CHEEK, RIGHT_EAR_TIP, RIGHT_EAR, RIGHT_EYE, RIGHT_MOUTH})
        @Retention(RetentionPolicy.SOURCE)
        @interface Type {
        }

        public android.graphics.PointF getPosition() {
            return this.mPosition;
        }

        @Type
        public int getType() {
            return this.mType;
        }

        public Landmark(android.graphics.PointF position, @Type int type) {
            this.mPosition = position;
            this.mType = type;
        }
    }


    public static class KangShiObj {
        public final String faceToken;

        public KangShiObj(String faceToken) {
            this.faceToken = faceToken;
        }
    }

}
