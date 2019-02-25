package com.instwall.base.utils;

import android.content.Context;
import android.content.SharedPreferences;


/**
 * Save Data To SharePreference Or Get Data from SharePreference
 * 通过SharedPreferences来存储数据，自定义类型
 */
public class SharedUtil {
    private String FileName = "FaceData";
    private SharedPreferences mData;

    public SharedUtil(Context ctx) {
        mData = ctx.getSharedPreferences(FileName, Context.MODE_MULTI_PROCESS);
    }

    public int readWorkState() {
        return mData.getInt("work_state", -1);
    }

    public void saveWorkState(int state) {
        mData.edit().putInt("work_state", state).apply();
    }

    public int readCameraDir() {
        return mData.getInt("camera_dir", 0);
    }

    public void saveCameraDir(int state) {
        mData.edit().putInt("camera_dir", state).apply();
    }

    public long readLocalFaceFileName() {
        return mData.getLong("face_name", 0);
    }

    public void saveLocalFaceFileName(long temp) {
        mData.edit().putLong("face_name", temp).apply();
    }
    // for debug window
    public void saveLocalDebugWindow(boolean isOpen) {
        mData.edit().putBoolean("debug_window", isOpen).apply();
    }

    public boolean readLocalDebugWindow() {
        return mData.getBoolean("debug_window", false);
    }

    // for arc engine init flag
    public void saveArcEngineState(boolean isInit) {
        mData.edit().putBoolean("arc_init", isInit).apply();
    }

    public boolean readArcEngineState() {
        return mData.getBoolean("arc_init", false);
    }
}
