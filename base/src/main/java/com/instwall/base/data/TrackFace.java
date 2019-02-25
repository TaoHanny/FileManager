package com.instwall.base.data;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by phcy on 6/23/17.
 */

public class TrackFace {
    // face identify state
    public static final int TRACK_AIDL = 7;
    public static final int TRACK_START = 4;
    public static final int TRACK_ATTENTION = 8;
    public static final int TRACK_IDENTIFY = 9;
    public static final int TRACK_END = 10;
    public static final int TRACK_MISS = 6;

    @IntDef({TRACK_START, TRACK_AIDL, TRACK_ATTENTION, TRACK_IDENTIFY, TRACK_END, TRACK_MISS})
    @Retention(RetentionPolicy.SOURCE)
    public @interface FaceTrackState {
    }

    public static String trackStateToString(@FaceTrackState int state) {
        switch (state) {
            case TRACK_AIDL:
                return "aidl";
            case TRACK_START:
                return "start";
            case TRACK_ATTENTION:
                return "attention";
            case TRACK_IDENTIFY:
                return "identify";
            case TRACK_END:
                return "end";
            case TRACK_MISS:
                return "miss";
            default:
                return "unknow-" + state;
        }
    }

    @FaceTrackState
    public int trackState = TRACK_AIDL;
    public int trackSameCount;
    public String localPic;
    public int trackId;
}
