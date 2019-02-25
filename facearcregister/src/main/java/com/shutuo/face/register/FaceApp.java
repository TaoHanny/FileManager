package com.shutuo.face.register;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.support.annotation.IntDef;
import android.util.Log;

import com.shutuo.face.register.BuildConfig;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ashy.earl.common.app.App;
import ashy.earl.common.task.MarkTracker;
import ashy.earl.common.task.MessageLoop;
import ashy.earl.common.util.L;
import ashy.earl.magicshell.clientapi.MagicShellClient;


public class FaceApp extends Application {
    public static final int LOOP_LOGICAL = 113;
    public static final int LOOP_SEARCH = 116;
    public static final int LOOP_API = 117;
    public static final int LOOP_DETECT=118;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LOOP_LOGICAL, LOOP_SEARCH, LOOP_API,LOOP_DETECT})
    @Target({ElementType.PARAMETER, ElementType.FIELD})
    public @interface Loop {
    }

    private boolean mLogToLogcat;
    private static MessageLoop sLogicalLoop;
    private static MessageLoop sSearchLoop;
    private static MessageLoop sApiLoop;
    private static MessageLoop sDetectLoop;
    private static File mCacheFile;
    private static boolean mEnableDumpPreviewToImageFile = true;

    @Override
    public void onCreate() {
        super.onCreate();
        String pkg = getPackageName();
        // Ignore other process
        App.appOnCreate(this);
        //do init FaceManager
        MagicShellClient.get().installMagicShell(this);
        // setup log
        setupLog();
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (mLogToLogcat == Log.isLoggable("all", Log.DEBUG)) return;
                setupLog();
            }
        }, new IntentFilter("ashy.earl.log"));

        mCacheFile = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
//      registDumpThreadReceiver();

    }


    private void setupLog() {
        if (BuildConfig.DEBUG || Log.isLoggable("all", Log.DEBUG)) {
            mLogToLogcat = true;
            L.Loggable loggable = new L.AndLog(new L.AndroidLog(), new L.FileLog());
            L.setupLogger(loggable);
            MarkTracker.enableLog(true);
        } else {
            mLogToLogcat = false;
            L.Loggable loggable = new L.FileLog();
            L.setupLogger(loggable);
            MarkTracker.enableLog(true);
        }
    }

}
