package com.instwall.base.im;

import org.json.JSONObject;

public interface ImListener {
    void onImStateChange(@ImClient.ImState int state);

    void onNewMessage(String from, String message, JSONObject maybeParsedJson);
}
