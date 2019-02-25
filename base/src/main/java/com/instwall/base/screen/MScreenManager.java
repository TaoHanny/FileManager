package com.instwall.base.screen;

import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.instwall.base.data.KvStorage;
import com.instwall.base.data.Module;
import com.instwall.base.data.ScreenInfo;
import com.instwall.base.utils.C;
import com.instwall.base.utils.JsonUtil;
import com.netcore.NetCore;
import com.netcore.NetCoreException;
import com.netcore.screen.IScreenCallback;
import com.netcore.screen.ScreenManager;

import org.json.JSONException;
import org.json.JSONObject;

import ashy.earl.common.app.App;
import ashy.earl.common.closure.EarlCall;
import ashy.earl.common.closure.Method0_0;
import ashy.earl.common.closure.Method1_0;
import ashy.earl.common.closure.Method2_0;
import ashy.earl.common.closure.Params0;
import ashy.earl.common.closure.Params1;
import ashy.earl.common.closure.Params2;
import ashy.earl.common.data.JsonParser;
import ashy.earl.common.data.ParserUtil;
import ashy.earl.common.task.MessageLoop;
import ashy.earl.common.task.RarTask;
import ashy.earl.common.task.annotation.DbThread;
import ashy.earl.common.util.L;
import ashy.earl.common.util.ModifyList;

import static ashy.earl.common.closure.Earl.bind;
import static com.instwall.base.data.ScreenInfo.RUN_LEVEL_BASIC;
import static com.instwall.base.data.ScreenInfo.RUN_LEVEL_FULL;
import static com.instwall.base.data.ScreenInfo.RUN_LEVEL_FUNCTION;
import static com.instwall.base.data.ScreenInfo.RUN_LEVEL_LOW;
import static com.instwall.base.data.ScreenInfo.bindStateFromString;
import static com.instwall.base.data.ScreenInfo.bindStateToString;
import static com.instwall.base.data.ScreenInfo.levelFromString;
import static com.instwall.base.data.ScreenInfo.levelToString;
import static com.instwall.base.data.ScreenInfo.linkmoveStateFromString;
import static com.instwall.base.data.ScreenInfo.linkmoveStateToString;
import static com.instwall.base.data.ScreenInfo.orientationFromString;
import static com.instwall.base.data.ScreenInfo.orientationToString;
import static com.instwall.base.data.ScreenInfo.playStateFromString;
import static com.instwall.base.data.ScreenInfo.playStateToString;


/**
 * Created by phcy on 8/11/17.
 */

public class MScreenManager extends Module {
    private static final String KEY_SCREEN_INFO = "screen_info";
    private static final String TAG = "MScreenManager";

    private ScreenManager mIpcScreenManager;
    private ScreenInfo mScreenInfo;
    private static MScreenManager sSelf;
    private final MessageLoop mDbLoop = App.getDbLoop();
    private final MessageLoop mMainLoop = App.getMainLoop();
    private final KvStorage mKvStorage = KvStorage.get();
    private boolean mNeedLoadData;
    private ModifyList<ScreenImpl> mScreenListeners = new ModifyList<>();
    private IScreenCallback mScreenInfoListener = new IScreenCallback() {
        @Override
        public void onConnect() {
            //do get screen info
            L.d(C.FACE, "%s~ Screen Client Connect...", TAG);
            mNeedLoadData = true;
            fetchScreenInfo();
        }

        @Override
        public void onScreenInfoStrChange() {
            L.d(C.FACE, "%s~  ScreenInfo Change...", TAG);
            mNeedLoadData = true;
            fetchScreenInfo();
        }

        @Override
        public void onScreenKeyChange() {

        }

        @Override
        public void onBaseScreenInfoChange() {

        }

        @Override
        public void onDisplayInfoChange() {

        }

        @Override
        public void onConfigInfoChange() {

        }

        @Override
        public void onStatusInfoChange() {

        }

        @Override
        public void onShopInfoChange() {

        }

        @Override
        public void onPincardInfoChange() {

        }

        @Override
        public void onScreenVerifyCodeChange(String verifyCode) {

        }


    };

    private MScreenManager() {
        mIpcScreenManager = ScreenManager.getInstance(App.getAppContext());
        mIpcScreenManager.setiScreenCallback(mScreenInfoListener, Looper.getMainLooper());
    }

    public static MScreenManager get() {
        if (sSelf != null) return sSelf;
        synchronized (MScreenManager.class) {
            if (sSelf == null) sSelf = new MScreenManager();
        }
        return sSelf;
    }

    public ScreenInfo getScreenInfo() {
        return mScreenInfo;
    }

    public String getBindState() {
        if (mScreenInfo == null) {
            return null;
        }
        return bindStateToString(mScreenInfo.bindState);
    }

    public void addScreenListener(ScreenImpl listener) {
        mScreenListeners.add(listener);
    }

    public void removeScreenListener(ScreenImpl listener) {
        mScreenListeners.remove(listener);
    }


    @Override
    public void start() {
        init();
    }

    protected void init() {
        mDbLoop.postTask(bind(readInitData, this).task());
    }

    @DbThread
    @EarlCall
    private void readInitData() {
        ScreenInfo screenInfo = mKvStorage.readData(KEY_SCREEN_INFO, CONVERT_SCREEN_INFO);
        mMainLoop.postTask(bind(didGotInitData, this, screenInfo).task());
    }

    @EarlCall
    private void didGotInitData(ScreenInfo screenInfo) {
        // Data invalidate
        L.d(C.FACE, "%s~ init finish, screenInfo:%s ", TAG, screenInfo);
        mScreenInfo = screenInfo;
        for (ScreenImpl l : mScreenListeners) {
            l.onScreenInfoChange(screenInfo);
        }
        mNeedLoadData = true;
        //    fetchScreenInfo();
        markInited();
    }

    private void fetchScreenInfo() {
        if (!mNeedLoadData) {
            L.d(C.FACE, "%s~ fetchScreenInfo no need load", TAG);
            return;
        }
        App.getBgLoop().postTask(new RarTask(bind(didGotServerScreenInfo, this),
                bind(obtainScreenInfo, this)));

    }


    @EarlCall
    private ScreenInfo didGotServerScreenInfo() {
        String jsonStr = mIpcScreenManager.getScreenInfoStr();
        if (TextUtils.isEmpty(jsonStr)) {
            L.d(C.FACE, "%s~ didGotServerScreenInfo info null", TAG);
            return null;
        }
        try {
            JSONObject screenObj = JsonUtil.getSingleData(new JSONObject(jsonStr));
            // do for need compatible
            return ParserUtil.parseObject(screenObj, PARSER_SERVER_SCREEN_INFO);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    @EarlCall
    private void obtainScreenInfo(ScreenInfo info, RuntimeException ex) {
        mScreenInfo = info;
        // do some thing
        mKvStorage.saveDataAsync(KEY_SCREEN_INFO, CONVERT_SCREEN_INFO, info, null);
        for (ScreenImpl l : mScreenListeners) {
            l.onScreenInfoChange(info);
        }
        mNeedLoadData = false;
    }

    private static final JsonParser<ScreenInfo> PARSER_SERVER_SCREEN_INFO = new JsonParser<ScreenInfo>() {
        @Nullable
        @Override
        public ScreenInfo onParse(@NonNull JSONObject json) {
            @ScreenInfo.BindState int bindState = bindStateFromString(json.optString("status"));
            @ScreenInfo.Orientation int orientation = ScreenInfo.ORIENTATION_LANDSCAPE;
            boolean autoStart = true;
            boolean supportRotate = true;
            @ScreenInfo.RunLevel int runLevel = RUN_LEVEL_BASIC;
            long shopId = 0;
            //
            JSONObject display = json.optJSONObject("display");
            if (display != null) {
                boolean vertical = "vertical".equals(display.optString("v_or_h"));
                boolean reverse_rotate = display.optInt("reverse_rotate") == 1;
                autoStart = display.optInt("automatic_start") == 1;
                orientation = vertical ?
                        (reverse_rotate ? ScreenInfo.ORIENTATION_REVERSE_PORTRAIT : ScreenInfo.ORIENTATION_PORTRAIT) :
                        (reverse_rotate ? ScreenInfo.ORIENTATION_REVERSE_LANDSCAPE : ScreenInfo.ORIENTATION_LANDSCAPE);
                supportRotate = display.optInt("physical_rotate") == 1;
            }
            //
            JSONObject hardware_info = json.optJSONObject("hardware_info");
            if (hardware_info != null) {
                switch (hardware_info.optInt("level", 0)) {
                    case 2:
                        runLevel = RUN_LEVEL_LOW;
                        break;
                    case 3:
                        runLevel = RUN_LEVEL_BASIC;
                        break;
                    case 4:
                        runLevel = RUN_LEVEL_FULL;
                        break;
                    case 5:
                        runLevel = RUN_LEVEL_FUNCTION;
                        break;
                    default:
                        runLevel = RUN_LEVEL_BASIC;
                        break;
                }
            }
            //

            String env = null;
            try {
                env = NetCore.getInstance(App.getAppContext(), true).getEnvNameEn();
            } catch (NetCoreException.InstwallServerNotReadyException e) {
                e.printStackTrace();
            }


            return new ScreenInfo(json.optString("screen_name"), json.optLong("screen_id"),
                    json.optString("screen_label"), json.optString("screen_key"),
                    json.optLong("bind_user_id"), null, orientation,
                    supportRotate, runLevel, shopId, bindState,
                    playStateFromString(json.optString("play_status")),
                    linkmoveStateFromString(json.optString("sg_status")),
                    json.optString("identified_tm"), autoStart, env);

        }

        @Nullable
        @Override
        public JSONObject toJson(@NonNull ScreenInfo obj) {
            return null;
        }

        @Override
        public ScreenInfo[] newArray(int size) {
            return new ScreenInfo[0];
        }
    };

    private static final JsonParser<ScreenInfo> PARSER_SCREEN_INFO = new JsonParser<ScreenInfo>() {

        @Nullable
        @Override
        public ScreenInfo onParse(@NonNull JSONObject json) {
            return new ScreenInfo(json.optString("screenName"), json.optLong("screenId"),
                    json.optString("screenLabel"), json.optString("screenKey"),
                    json.optLong("screenBindUserId"), json.optString("screenDid"),
                    orientationFromString(json.optString("screenOrientation")),
                    json.optBoolean("screenSupportRotate"),
                    levelFromString(json.optString("screenRunLevel")),
                    json.optLong("screenShopId"),
                    bindStateFromString(json.optString("bindState")),
                    playStateFromString(json.optString("playState")),
                    linkmoveStateFromString(json.optString("linkmoveState")),
                    json.optString("contentVersion"),
                    json.optBoolean("configAutoStart"), json.optString("env")
            );
        }

        @Nullable
        @Override
        public JSONObject toJson(@NonNull ScreenInfo data) {
            try {
                return new JSONObject().put("screenName", data.screenName)
                        .put("screenId", data.screenId)
                        .put("screenLabel", data.screenLabel)
                        .put("screenKey", data.screenKey)
                        .put("screenBindUserId", data.screenBindUserId)
                        .put("screenDid", data.screenDid).put("screenOrientation",
                                orientationToString(
                                        data.screenOrientation))
                        .put("screenSupportRotate", data.screenSupportRotate)
                        .put("screenRunLevel", levelToString(data.screenRunLevel))
                        .put("screenShopId", data.screenShopId)
                        .put("bindState", bindStateToString(data.bindState))
                        .put("playState", playStateToString(data.playState))
                        .put("linkmoveState",
                                linkmoveStateToString(data.linkmoveState))
                        .put("contentVersion", data.contentVersion)
                        .put("configAutoStart", data.configAutoStart)
                        .put("env", data.env);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public ScreenInfo[] newArray(int size) {
            return new ScreenInfo[size];
        }
    };
    private static KvStorage.DataConvert<ScreenInfo> CONVERT_SCREEN_INFO = new KvStorage.JsonConvert<>(PARSER_SCREEN_INFO);

    public interface ScreenImpl {


        void onScreenInfoChange(ScreenInfo info);
    }

    private static final Method0_0<MScreenManager, Void> readInitData
            = new Method0_0<MScreenManager, Void>(MScreenManager.class, "readInitData") {
        @Override
        public Void run(MScreenManager target, @NonNull Params0 params) {
            target.readInitData();
            return null;
        }
    };
    private static final Method1_0<MScreenManager, Void, ScreenInfo> didGotInitData
            = new Method1_0<MScreenManager, Void, ScreenInfo>(MScreenManager.class, "didGotInitData") {
        @Override
        public Void run(MScreenManager target, @NonNull Params1<ScreenInfo> params) {
            target.didGotInitData(params.p1);
            return null;
        }
    };
    private static final Method0_0<MScreenManager, ScreenInfo> didGotServerScreenInfo
            = new Method0_0<MScreenManager, ScreenInfo>(MScreenManager.class, "didGotServerScreenInfo") {
        @Override
        public ScreenInfo run(MScreenManager target, @NonNull Params0 params) {
            return target.didGotServerScreenInfo();
        }
    };
    private static final Method2_0<MScreenManager, Void, ScreenInfo, RuntimeException> obtainScreenInfo
            = new Method2_0<MScreenManager, Void, ScreenInfo, RuntimeException>(MScreenManager.class, "obtainScreenInfo") {
        @Override
        public Void run(MScreenManager target, @NonNull Params2<ScreenInfo, RuntimeException> params) {
            target.obtainScreenInfo(params.p1, params.p2);
            return null;
        }
    };
}
