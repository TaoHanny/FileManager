package com.instwall.base.im;

import android.support.annotation.BinderThread;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.instwall.base.utils.C;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import ashy.earl.common.app.App;
import ashy.earl.common.closure.EarlCall;
import ashy.earl.common.closure.Method1_0;
import ashy.earl.common.closure.Method3_0;
import ashy.earl.common.closure.Params1;
import ashy.earl.common.closure.Params3;
import ashy.earl.common.task.MessageLoop;
import ashy.earl.common.util.L;
import ashy.earl.common.util.ModifyList;

import static ashy.earl.common.closure.Earl.bind;

public class ImClient {
    private static final String TAG = "ImClient";
    private static final String JSON_START = "##json##:";
    public static final int IM_STATE_SERVICE_DISCONNECT = 1;
    public static final int IM_STATE_LOG_OUT = 2;
    public static final int IM_STATE_LOGING = 3;
    public static final int IM_STATE_READY = 4;
    public static final int IM_STATE_SERVICE_CONNECTING = 5;
    @ImState
    private int mImState = IM_STATE_SERVICE_DISCONNECT;
    private ImClientImpl mImClientInner;
    private final MessageLoop mMainLoop = App.getMainLoop();
    private static ImClient sSelf;
    private ImMsgInterupter mImMsgInterupter;
    private ModifyList<ImListener> mImListeners = new ModifyList<>();
    private String mCmdFrom;

    @IntDef({IM_STATE_SERVICE_DISCONNECT, IM_STATE_LOG_OUT, IM_STATE_LOGING, IM_STATE_READY,
            IM_STATE_SERVICE_CONNECTING})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ImState {
    }

    public interface ImMsgInterupter {
        @BinderThread
        boolean handleMsg(String from, String msg);
    }

    public static String imStateToString(@ImState int state) {
        switch (state) {
            case IM_STATE_LOGING:
                return "loging";
            case IM_STATE_LOG_OUT:
                return "log-out";
            case IM_STATE_READY:
                return "ready";
            case IM_STATE_SERVICE_DISCONNECT:
                return "service-disconnect";
            case IM_STATE_SERVICE_CONNECTING:
                return "service-connecting";
            default:
                return "unknow-" + state;
        }
    }

    public static ImClient get() {
        if (sSelf != null) return sSelf;
        synchronized (ImClient.class) {
            if (sSelf == null) sSelf = new ImClient();
        }
        return sSelf;
    }

    private ImClient() {
        ImListener imListener = new ImListener() {
            @Override
            public void onImStateChange(int state) {
                mMainLoop.postTask(bind(imStateChanged, ImClient.this, state).task());
            }

            @Override
            public void onNewMessage(String from, String message, JSONObject maybeParsedJson) {
                if (TextUtils.isEmpty(from) || TextUtils.isEmpty(message)) return;
                if (L.loggable(C.FACE, L.DEBUG)) {
                    L.d(C.FACE, "%s~ new im message, from:%s, message:%s", TAG, from, message);
                }
                mCmdFrom = from;
                // Msg interupter.
                if (mImMsgInterupter != null && mImMsgInterupter.handleMsg(from, message)) return;
                JSONObject parsedJson = null;
                if (message.contains(JSON_START)) {
                    int start = message.indexOf(JSON_START);
                    int end = message.length();
                    if (start == -1) {
                        if (L.loggable(C.FACE, L.DEBUG)) {
                            L.e(C.FACE, "%s~ handleMessage -> normal chat msg", TAG);
                        }
                    } else {
                        String jsonStr = message.substring(start + JSON_START.length(), end - 2);
                        try {
                            parsedJson = new JSONObject(jsonStr);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                mMainLoop.postTask(
                        bind(newImMessage, ImClient.this, from, message, parsedJson).task());
            }
        };
        mImClientInner = new ImClientImpl(imListener);
    }

    public void setImMsgInterupter(ImMsgInterupter l) {
        mImMsgInterupter = l;
    }

    public void addImListener(ImListener l) {
        mImListeners.add(l);
    }

    public void removeImListener(ImListener l) {
        mImListeners.remove(l);
    }

    @EarlCall
    private void imStateChanged(int state) {
        if (mImState == state) return;
        if (L.loggable(C.FACE, L.DEBUG)) {
            L.d(C.FACE, "%s~ new im state:%s -> %s", TAG, imStateToString(mImState),
                    imStateToString(state));
        }
        mImState = state;
        for (ImListener l : mImListeners) l.onImStateChange(state);
    }

    @EarlCall
    private void newImMessage(String from, String message, JSONObject parsedJson) {
        for (ImListener l : mImListeners) {
            l.onNewMessage(from, message, parsedJson);
        }
    }

    // ===================================================================
    @ImState
    public int getImState() {
        return mImState;
    }

    public void sendMessage(String to, String message) {
        if (L.loggable(C.FACE, L.DEBUG)) {
            L.d(C.FACE, "%s~ sendMessage, to:%s, message:%s", TAG, to, message);
        }
        if (TextUtils.isEmpty(to)) {
            to = mCmdFrom;
        }
        mImClientInner.sendMessage(to, message);
    }

    private static final Method1_0<ImClient, Void, Integer> imStateChanged = new Method1_0<ImClient, Void, Integer>(
            ImClient.class, "imStateChanged") {
        @Override
        public Void run(ImClient target, @NonNull Params1<Integer> params) {
            target.imStateChanged(u(params.p1));
            return null;
        }
    };
    private static final Method3_0<ImClient, Void, String, String, JSONObject> newImMessage = new Method3_0<ImClient, Void, String, String, JSONObject>(
            ImClient.class, "newImMessage") {
        @Override
        public Void run(ImClient target, @NonNull Params3<String, String, JSONObject> params) {
            target.newImMessage(params.p1, params.p2, params.p3);
            return null;
        }
    };
}
