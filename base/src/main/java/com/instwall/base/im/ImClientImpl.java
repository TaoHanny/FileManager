package com.instwall.base.im;

import android.os.RemoteException;
import android.text.TextUtils;

import com.instwall.server.client.ImClient;

import org.json.JSONException;
import org.json.JSONObject;

import ashy.earl.common.app.App;

import static com.instwall.base.im.ImClient.IM_STATE_LOGING;
import static com.instwall.base.im.ImClient.IM_STATE_LOG_OUT;
import static com.instwall.base.im.ImClient.IM_STATE_READY;
import static com.instwall.server.client.ImClient.STATUS_CONNECTING;
import static com.instwall.server.client.ImClient.STATUS_DISCONNECTED;
import static com.instwall.server.client.ImClient.STATUS_LOGINED;
import static com.instwall.server.client.ImClient.STATUS_LOGINING;
import static com.instwall.server.client.ImClient.STATUS_RECONNECTING;

public class ImClientImpl {
    private final ImListener mImListener;
    private final ImClient mServerImClient;

    public ImClientImpl(ImListener imListener) {
        mImListener = imListener;
        mServerImClient = ImClient.getInstance(App.getAppContext());
        com.instwall.server.client.ImListener serverListener = new com.instwall.server.client.ImListener() {
            @Override
            public void onConnect() {
                // AIDL  connect
                try {
                    notifyStateChanged(mServerImClient.getImStatus());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onDisconnect() {
                // AIDL disconnect
                notifyStateChanged(STATUS_DISCONNECTED);
            }

            @Override
            public void onImStatusChange(String oldStatus, String newStatus) {
                // Im status changed
                notifyStateChanged(newStatus);
            }

            @Override
            public void onReceiveMessage(String from, String message) {
                // receive Im message
                notifyImMsg(from, message);
            }
        };
        mServerImClient.setImListener(serverListener, null);
    }

    private void notifyStateChanged(String status) {
        if (TextUtils.isEmpty(status)) return;
        int imState = IM_STATE_LOG_OUT;
        if (STATUS_DISCONNECTED.equals(status)) {
            imState = IM_STATE_LOG_OUT;
        } else if (STATUS_LOGINING.equals(status) ||
                STATUS_CONNECTING.equals(status) ||
                STATUS_RECONNECTING.equals(status)) {
            imState = IM_STATE_LOGING;
        } else if (STATUS_LOGINED.equals(status)) {
            imState = IM_STATE_READY;
        }
        mImListener.onImStateChange(imState);
    }

    private void notifyImMsg(String from, String msg) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(msg);
            msg = "[@ds_control@][[##json##:" + msg + "]]";
        } catch (JSONException ignore) {
        }
        mImListener.onNewMessage(from, msg, jsonObject);
    }

    public void sendMessage(String to, String message) {
        try {
            mServerImClient.sendMessage(to, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
