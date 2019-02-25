package com.instwall.base.data;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ScreenInfo {
    // Bind state
    public static final int STATE_BIND = 1;
    public static final int STATE_UNBIND = 2;
    private static final String STATE_BIND_STR = "bind";
    private static final String STATE_UNBIND_STR = "unbind";
    // Play state
    public static final int PLAY_STATE_START = 1;
    public static final int PLAY_STATE_STOP = 2;
    private static final String PLAY_STATE_START_STR = "start";
    private static final String PLAY_STATE_STOP_STR = "stop";
    // Linkmove state
    public static final int LINKMOVE_STATE_NOT_JOIN = 1;
    public static final int LINKMOVE_STATE_JOINED = 2;
    private static final String LINKMOVE_STATE_NOT_JOIN_STR = "not-join";
    private static final String LINKMOVE_STATE_JOINED_STR = "joined";
    // Orientation
    public static final int ORIENTATION_LANDSCAPE = 1;
    public static final int ORIENTATION_PORTRAIT = 2;
    public static final int ORIENTATION_REVERSE_LANDSCAPE = 3;
    public static final int ORIENTATION_REVERSE_PORTRAIT = 4;
    private static final String ORIENTATION_LANDSCAPE_STR = "landscape";
    private static final String ORIENTATION_PORTRAIT_STR = "portrail";
    private static final String ORIENTATION_REVERSE_LANDSCAPE_STR = "reverse-landscape";
    private static final String ORIENTATION_REVERSE_PORTRAIT_STR = "reverse-portrail";

    /**
     * Low level, no video & no html5 page, only play picture
     */
    public static final int RUN_LEVEL_LOW = 1;
    /**
     * Basic level, full screen video & no html5 page with picture instead.
     */
    public static final int RUN_LEVEL_BASIC = 2;
    /**
     * Full level, html5 page & html page video support(All function).
     */
    public static final int RUN_LEVEL_FULL = 3;
    /**
     * Function level, html5 page & no video support.
     */
    public static final int RUN_LEVEL_FUNCTION = 4;
    private static final String RUN_LEVEL_LOW_STR = "low";
    private static final String RUN_LEVEL_BASIC_STR = "basic";
    private static final String RUN_LEVEL_FULL_STR = "full";
    private static final String RUN_LEVEL_FUNCTION_STR = "function";
    // Screen info
    public final String screenName;
    public final long screenId;
    public final String screenLabel;
    public final String screenKey;
    public final long screenBindUserId;
    public final String screenDid;
    @Orientation
    public final int screenOrientation;
    public final boolean screenSupportRotate;
    @RunLevel
    public final int screenRunLevel;
    public final long screenShopId;
    // Play state
    @BindState
    public final int bindState;
    @PlayState
    public final int playState;
    @LinkmoveState
    public final int linkmoveState;
    public final String contentVersion;
    // Config info
    public final boolean configAutoStart;
    //
    public final String env;
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({STATE_BIND, STATE_UNBIND})
    public @interface BindState {
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({PLAY_STATE_START, PLAY_STATE_STOP})
    public @interface PlayState {
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LINKMOVE_STATE_NOT_JOIN, LINKMOVE_STATE_JOINED})
    public @interface LinkmoveState {
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ORIENTATION_LANDSCAPE, ORIENTATION_PORTRAIT, ORIENTATION_REVERSE_LANDSCAPE,
            ORIENTATION_REVERSE_PORTRAIT})
    public @interface Orientation {
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({RUN_LEVEL_LOW, RUN_LEVEL_BASIC, RUN_LEVEL_FULL, RUN_LEVEL_FUNCTION})
    public @interface RunLevel {
    }

    public static String bindStateToString(@BindState int state) {
        switch (state) {
            case STATE_BIND:
                return STATE_BIND_STR;
            case STATE_UNBIND:
                return STATE_UNBIND_STR;
            default:
                return "unknow-" + state;
        }
    }

    @BindState
    public static int bindStateFromString(String state) {
        if (state == null) return STATE_UNBIND;
        switch (state) {
            case STATE_BIND_STR:
                return STATE_BIND;
            case STATE_UNBIND_STR:
                return STATE_UNBIND;
            default:
                return STATE_UNBIND;
        }
    }

    public static String playStateToString(@PlayState int state) {
        switch (state) {
            case PLAY_STATE_START:
                return PLAY_STATE_START_STR;
            case PLAY_STATE_STOP:
                return PLAY_STATE_STOP_STR;
            default:
                return "unknow-" + state;
        }
    }

    @PlayState
    public static int playStateFromString(String state) {
        if (state == null) return PLAY_STATE_STOP;
        switch (state) {
            case PLAY_STATE_START_STR:
                return PLAY_STATE_START;
            case PLAY_STATE_STOP_STR:
                return PLAY_STATE_STOP;
            default:
                return PLAY_STATE_STOP;
        }
    }

    public static String linkmoveStateToString(@LinkmoveState int state) {
        switch (state) {
            case LINKMOVE_STATE_JOINED:
                return LINKMOVE_STATE_JOINED_STR;
            case LINKMOVE_STATE_NOT_JOIN:
                return LINKMOVE_STATE_NOT_JOIN_STR;
            default:
                return "unknow-" + state;
        }
    }

    @LinkmoveState
    public static int linkmoveStateFromString(String state) {
        if (state == null) return LINKMOVE_STATE_NOT_JOIN;
        switch (state) {
            case LINKMOVE_STATE_JOINED_STR:
                return LINKMOVE_STATE_JOINED;
            case LINKMOVE_STATE_NOT_JOIN_STR:
                return LINKMOVE_STATE_NOT_JOIN;
            default:
                return LINKMOVE_STATE_NOT_JOIN;
        }
    }

    public static String orientationToString(@Orientation int state) {
        switch (state) {
            case ORIENTATION_LANDSCAPE:
                return ORIENTATION_LANDSCAPE_STR;
            case ORIENTATION_PORTRAIT:
                return ORIENTATION_PORTRAIT_STR;
            case ORIENTATION_REVERSE_LANDSCAPE:
                return ORIENTATION_REVERSE_LANDSCAPE_STR;
            case ORIENTATION_REVERSE_PORTRAIT:
                return ORIENTATION_REVERSE_PORTRAIT_STR;
            default:
                return "unknow-" + state;
        }
    }

    @Orientation
    public static int orientationFromString(String state) {
        if (state == null) return ORIENTATION_LANDSCAPE;
        switch (state) {
            case ORIENTATION_LANDSCAPE_STR:
                return ORIENTATION_LANDSCAPE;
            case ORIENTATION_PORTRAIT_STR:
                return ORIENTATION_PORTRAIT;
            case ORIENTATION_REVERSE_LANDSCAPE_STR:
                return ORIENTATION_REVERSE_LANDSCAPE;
            case ORIENTATION_REVERSE_PORTRAIT_STR:
                return ORIENTATION_REVERSE_PORTRAIT;
            default:
                return ORIENTATION_LANDSCAPE;
        }
    }


    public static String levelToString(@RunLevel int state) {
        switch (state) {
            case RUN_LEVEL_BASIC:
                return RUN_LEVEL_BASIC_STR;
            case RUN_LEVEL_FULL:
                return RUN_LEVEL_FULL_STR;
            case RUN_LEVEL_FUNCTION:
                return RUN_LEVEL_FUNCTION_STR;
            case RUN_LEVEL_LOW:
                return RUN_LEVEL_LOW_STR;
            default:
                return "unknow-" + state;
        }
    }

    @RunLevel
    public static int levelFromString(String state) {
        if (state == null) return RUN_LEVEL_BASIC;
        switch (state) {
            case RUN_LEVEL_BASIC_STR:
                return RUN_LEVEL_BASIC;
            case RUN_LEVEL_FULL_STR:
                return RUN_LEVEL_FULL;
            case RUN_LEVEL_FUNCTION_STR:
                return RUN_LEVEL_FUNCTION;
            case RUN_LEVEL_LOW_STR:
                return RUN_LEVEL_LOW;
            default:
                return RUN_LEVEL_BASIC;
        }
    }


    public ScreenInfo(String screenName, long screenId, String screenLabel, String screenKey,
                      long screenBindUserId, String screenDid, int screenOrientation,
                      boolean screenSupportRotate, @RunLevel int screenRunLevel, long screenShopId,
                      @BindState int bindState, @PlayState int playState, @LinkmoveState int linkmoveState,
                      String contentVersion, boolean configAutoStart, String env ) {
        this.screenName = screenName;
        this.screenId = screenId;
        this.screenLabel = screenLabel;
        this.screenKey = screenKey;
        this.screenBindUserId = screenBindUserId;
        this.screenDid = screenDid;
        this.screenOrientation = screenOrientation;
        this.screenSupportRotate = screenSupportRotate;
        this.screenRunLevel = screenRunLevel;
        this.screenShopId = screenShopId;
        this.bindState = bindState;
        this.playState = playState;
        this.linkmoveState = linkmoveState;
        this.contentVersion = contentVersion;
        this.configAutoStart = configAutoStart;
        this.env = env;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ScreenInfo that = (ScreenInfo) o;

        if (screenId != that.screenId) return false;
        if (screenBindUserId != that.screenBindUserId) return false;
        if (screenOrientation != that.screenOrientation) return false;
        if (screenSupportRotate != that.screenSupportRotate) return false;
        if (screenRunLevel != that.screenRunLevel) return false;
        if (screenShopId != that.screenShopId) return false;
        if (bindState != that.bindState) return false;
        if (playState != that.playState) return false;
        if (linkmoveState != that.linkmoveState) return false;
        if (configAutoStart != that.configAutoStart) return false;
        if (screenName != null ? !screenName.equals(that.screenName) : that.screenName != null)
            return false;
        if (screenLabel != null ? !screenLabel.equals(that.screenLabel) : that.screenLabel != null)
            return false;
        if (screenKey != null ? !screenKey.equals(that.screenKey) : that.screenKey != null)
            return false;
        if (screenDid != null ? !screenDid.equals(that.screenDid) : that.screenDid != null)
            return false;
        if (contentVersion != null ? !contentVersion.equals(that.contentVersion) : that.contentVersion != null)
            return false;
        return env != null ? env.equals(that.env) : that.env == null;
    }

    @Override
    public int hashCode() {
        int result = screenName != null ? screenName.hashCode() : 0;
        result = 31 * result + (int) (screenId ^ (screenId >>> 32));
        result = 31 * result + (screenLabel != null ? screenLabel.hashCode() : 0);
        result = 31 * result + (screenKey != null ? screenKey.hashCode() : 0);
        result = 31 * result + (int) (screenBindUserId ^ (screenBindUserId >>> 32));
        result = 31 * result + (screenDid != null ? screenDid.hashCode() : 0);
        result = 31 * result + screenOrientation;
        result = 31 * result + (screenSupportRotate ? 1 : 0);
        result = 31 * result + screenRunLevel;
        result = 31 * result + (int) (screenShopId ^ (screenShopId >>> 32));
        result = 31 * result + bindState;
        result = 31 * result + playState;
        result = 31 * result + linkmoveState;
        result = 31 * result + (contentVersion != null ? contentVersion.hashCode() : 0);
        result = 31 * result + (configAutoStart ? 1 : 0);
        result = 31 * result + (env != null ? env.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ScreenInfo{" +
                "screenName='" + screenName + '\'' +
                ", screenId=" + screenId +
                ", screenLabel='" + screenLabel + '\'' +
                ", screenKey='" + screenKey + '\'' +
                ", screenBindUserId=" + screenBindUserId +
                ", screenDid='" + screenDid + '\'' +
                ", screenOrientation=" + screenOrientation +
                ", screenSupportRotate=" + screenSupportRotate +
                ", screenRunLevel=" + screenRunLevel +
                ", screenShopId=" + screenShopId +
                ", bindState=" + bindState +
                ", playState=" + playState +
                ", linkmoveState=" + linkmoveState +
                ", contentVersion='" + contentVersion + '\'' +
                ", configAutoStart=" + configAutoStart +
                ", env='" + env + '\'' +
                '}';
    }
}
