package com.instwall.base.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtil {
    public static String getString(JSONObject object, String name, String defValue) {
        String result = defValue;
        try {
            result = object.getString(name);
        } catch (JSONException e) {
        }
        return result;
    }

    public static long getLong(JSONObject object, String name, long defValue) {
        long result = defValue;
        try {
            result = object.getLong(name);
        } catch (JSONException e) {
        }
        return result;
    }

    public static boolean getBoolean(JSONObject object, String name, boolean defValue) {
        boolean result = defValue;
        try {
            result = object.getBoolean(name);
        } catch (JSONException e) {
        }
        return result;
    }

    public static double getDouble(JSONObject object, String name, double defValue) {
        double result = defValue;
        try {
            result = object.getDouble(name);
        } catch (JSONException e) {
        }
        return result;
    }

    public static float getFloat(JSONObject object, String name, float defValue) {
        float result = defValue;
        try {
            result = (float) object.getDouble(name);
        } catch (JSONException e) {
        }
        return result;
    }

    public static int getInt(JSONObject object, String name, int defValue) {
        int result = defValue;
        try {
            result = object.getInt(name);
        } catch (JSONException e) {
        }
        return result;
    }

    public static JSONObject getJsonObj(JSONObject object, String name, JSONObject defValue) {
        JSONObject result = defValue;
        try {
            result = object.getJSONObject(name);
        } catch (JSONException e) {
        }
        return result;
    }

    public static JSONArray getJsonArray(JSONObject object, String name, JSONArray defValue) {
        JSONArray result = defValue;
        try {
            result = object.getJSONArray(name);
        } catch (JSONException e) {
        }
        return result;
    }

    public static JSONArray getJsonArray(JSONArray object, int index, JSONArray defValue) {
        JSONArray result = defValue;
        try {
            result = object.getJSONArray(index);
        } catch (JSONException e) {
        }
        return result;
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////
    // Get from JSONArray
    public static JSONObject getJsonObj(JSONArray object, int index, JSONObject defValue) {
        JSONObject result = defValue;
        try {
            result = object.getJSONObject(index);
        } catch (JSONException e) {
        }
        return result;
    }

    public static String getString(JSONArray object, int index, String defValue) {
        String result = defValue;
        try {
            result = object.getString(index);
        } catch (JSONException e) {
        }
        return result;
    }

    public static int getInt(JSONArray object, int index, int defValue) {
        int result = defValue;
        try {
            result = object.getInt(index);
        } catch (JSONException e) {
        }
        return result;
    }

    public static float getFloat(JSONArray object, int index, float defValue) {
        float result = defValue;
        try {
            result = (float) object.getDouble(index);
        } catch (JSONException e) {
        }
        return result;
    }

    public static JSONObject getSingleData(JSONObject jsonObject) {
        JSONArray dataArray = JsonUtil.getJsonArray(jsonObject, "data", null);
        if (dataArray == null || dataArray.length() == 0) return null;
        JSONObject dataObj = JsonUtil.getJsonObj(dataArray, 0, null);
        return dataObj;
    }


}
