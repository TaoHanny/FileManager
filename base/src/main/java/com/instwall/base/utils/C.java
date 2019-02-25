package com.instwall.base.utils;

import android.net.Uri;

/**
 * Created by phcy on 17-10-8.
 */

public class C {
    public static final String FACE = "face";
    public static final String FACE_API = "face_api";
    public static final String FACE_DB="face_db";
    public static String PREVIEW_SHOT_PATH = "/sdcard/IM_Current_Shot.jpg";

    // for provider config
    public static final Uri CONFIG_URI = Uri.parse("content://com.instwall.LkSetContentProvider.set");
    public static final String FACE_CONFIG_KEY = "faceidentifydebug";
    public static final String FACE_IDENTITY_KEY = "faceidentifyflg";
    public static final String PARAMS_KEY = "params_key";
    public static final String PARAMS_CLOSE_CAMERA = "close_camera";
    public static final String PARAMS_RESUME_CAMERA = "resume_camera";
    public static final String PARAMS_COUNT_START = "count_start";
    public static final String PARAMS_COUNT_STOP = "count_stop";
    public static final String PARAMS_IDENTIFY_START = "identify_stop";
    public static final String PARMAS_IDENTIFY_STOP = "identify_stop";


    //  intent params change
    public static final Uri CONTENT_URI = Uri.parse("content://com.instwall.launch.MContentProvider.set");
    public static final String KEY_WEL_SAMPLING = "wel_sampling";
    public static final String KEY_TIMELY_PUSH = "timely_push";// 购物助手 APP
    public static final String KEY_WEL_REPORT = "wel_report";
    public static final String KEY_RT_REPORT="rt_report";
    public static final String KEY_WEL_PUSH = "wel_push"; // 屏幕推荐控制
    public static final String KEY_WEL_SEARCH="wel_search";
}
