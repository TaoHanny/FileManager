package com.instwall.base;

import android.support.annotation.IntDef;
import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class FaceConfig {
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

    // do for detect engine face ++
    public static final int MODULE_FOR_FACE_A = 6;
    public static final int MODULE_FOR_ST = 7;
    public static final int MODULE_FOR_BAIDU = 8;
    public static final int MODULE_FOR_ARC=9;
    @IntDef({MODULE_FOR_FACE_A, MODULE_FOR_ST, MODULE_FOR_BAIDU,MODULE_FOR_ARC})
    @Retention(RetentionPolicy.SOURCE)
    public @interface EngineModule {
    }

    public static String moduleToString(@EngineModule int module) {
        switch (module) {
            case MODULE_FOR_FACE_A:
                return "face ++  cloud api";
            case MODULE_FOR_ST:
                return "ShangTang local so";
            case MODULE_FOR_BAIDU:
                return "baidu cloud api";
            case MODULE_FOR_ARC:
                return "arc engine local so";
            default:
                return "unknow-" + module;
        }
    }


    public static int moduleToInteger(String module) {
          if("face ++  cloud api".equals(module)) {
              return MODULE_FOR_FACE_A;
          }else if("ShangTang local so".equals(module)){
              return MODULE_FOR_ST;
          }else if("baidu cloud api".equals(module)){
              return MODULE_FOR_BAIDU;
          }else if("arc engine local so".equals(module)){
              return MODULE_FOR_ARC;
          }else
              return -1;
    }

    public static final int ERROR_ENGINE_CLOUD_API = 112;
    public static final int ERROR_SAMPLING = 113;
    public static final int ERROR_ENGINE_IDENTIFY = 115;
    public static final int ERROR_LOCAL_LIB = 116;
    public static final int ERROR_SERVICE_NOT_READY = 2;
    public static final int ERROR_REMOTE = 3;
    public static final int ERROR_PARAMS = 6;
    public static final int ERROR_BREAK_BY_NEW_TASK = 7;
    // for detail cloud api
    public static final int ERROR_API_AUTHOR = 401;
    public static final int ERROR_API_IMAGE_SIZE_INVALID = 400;
    public static final int ERROR_API_INTERNAL = 500;// face ++ server error
    public static final int ERROR_API_ARGUMENT = 402; //params error
    public static final int ERROR_API_EMPTY_FACESET = 403;// search face ont in this faceset
    public static final int ERROR_API_CONCURRENCY_LIMIT_EXCEEDED = 405; // concurrency limit exceeded
    public static final int ERROR_API_FACE_QUOTA_EXCEEDED = 406;// face add quota exceed

    @IntDef({ERROR_ENGINE_CLOUD_API, ERROR_SAMPLING, ERROR_ENGINE_IDENTIFY,
            ERROR_LOCAL_LIB, ERROR_SERVICE_NOT_READY, ERROR_REMOTE, ERROR_PARAMS, ERROR_BREAK_BY_NEW_TASK,
            ERROR_API_AUTHOR, ERROR_API_IMAGE_SIZE_INVALID, ERROR_API_INTERNAL, ERROR_API_ARGUMENT,
            ERROR_API_EMPTY_FACESET, ERROR_API_CONCURRENCY_LIMIT_EXCEEDED, ERROR_API_FACE_QUOTA_EXCEEDED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Error {
    }

    public static String errorToString(@Error int type) {
        switch (type) {
            case ERROR_REMOTE:
                return "remote";
            case ERROR_SERVICE_NOT_READY:
                return "service-not-ready";
            case ERROR_PARAMS:
                return "params";
            case ERROR_BREAK_BY_NEW_TASK:
                return "break-by-new-task";
            case ERROR_ENGINE_CLOUD_API:
                return "engine cloud api";
            case ERROR_SAMPLING:
                return "engine sampling";
            case ERROR_ENGINE_IDENTIFY:
                return "engine identify";
            case ERROR_LOCAL_LIB:
                return "engine local so";
            case ERROR_API_AUTHOR:
                return "engine author error";
            case ERROR_API_IMAGE_SIZE_INVALID:
                return "use image file size invalid";
            case ERROR_API_INTERNAL:
                return "server internal error";
            case ERROR_API_ARGUMENT:
                return "request argument error";
            case ERROR_API_EMPTY_FACESET:
                return "search face not in face set";
            case ERROR_API_CONCURRENCY_LIMIT_EXCEEDED:
                return "concurrency limit exceeded";
            case ERROR_API_FACE_QUOTA_EXCEEDED:
                return "face count quota exceeded";
            default:
                return "unknow-" + type;
        }
    }


    // service item
    public static final String FACE_COUNT = "face_count";
    public static final String FACE_SEARCH = "face_search";
    public static final String FACE_SAMPLING = "face_sampling";

    @StringDef({FACE_COUNT, FACE_SEARCH, FACE_SAMPLING})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ServiceItem {
    }
}
