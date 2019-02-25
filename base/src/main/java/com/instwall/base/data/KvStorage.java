package com.instwall.base.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import com.instwall.base.utils.C;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import ashy.earl.common.app.App;
import ashy.earl.common.closure.Closure2_0;
import ashy.earl.common.closure.EarlCall;
import ashy.earl.common.closure.Method0_0;
import ashy.earl.common.closure.Method1_0;
import ashy.earl.common.closure.Method2_0;
import ashy.earl.common.closure.Method3_0;
import ashy.earl.common.closure.Params0;
import ashy.earl.common.closure.Params1;
import ashy.earl.common.closure.Params2;
import ashy.earl.common.closure.Params3;
import ashy.earl.common.data.JsonParser;
import ashy.earl.common.data.ParserUtil;
import ashy.earl.common.task.MessageLoop;
import ashy.earl.common.task.RarTask;
import ashy.earl.common.task.Task;
import ashy.earl.common.task.annotation.DbThread;
import ashy.earl.common.util.L;

import static ashy.earl.common.closure.Earl.bind;

public class KvStorage {
    private static final String TAG = "KvStorage";
    public static final String VALUE_TRUE = "true";
    public static final String VALUE_FALSE = "false";
    private static KvStorage sSelf;
    private InnerDb mInnerDb;
    private final MessageLoop mDbLoop = App.getDbLoop();
    private FirstCreateListener mFirstCreateListener;

    public interface FirstCreateListener {
        void onStorageFirstCreate();
    }

    public interface DataConvert<T> {
        @Nullable
        String asString(@Nullable T data);

        @Nullable
        T asData(@Nullable String data);
    }

    public static class JsonConvert<T> implements DataConvert<T> {
        private final JsonParser<T> mParser;

        public JsonConvert(JsonParser<T> parser) {
            mParser = parser;
        }

        @Nullable
        @Override
        public String asString(@Nullable T data) {
            if (data == null) return null;
            JSONObject json = mParser.toJson(data);
            return json == null ? null : json.toString();
        }

        @Nullable
        @Override
        public T asData(@Nullable String data) {
            if (data == null) return null;
            try {
                return mParser.onParse(new JSONObject(data));
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    private KvStorage() {
    }

    public static KvStorage get() {
        if (sSelf != null) return sSelf;
        synchronized (KvStorage.class) {
            if (sSelf == null) sSelf = new KvStorage();
        }
        return sSelf;
    }

    public void setupFirstCreateListener(FirstCreateListener listener) {
        if (mInnerDb != null) {
            throw new IllegalStateException("setupFirstCreateListener after read or write!");
        }
        if (mFirstCreateListener != null) {
            throw new IllegalStateException("FirstCreateListener already setup!");
        }
        mFirstCreateListener = listener;
    }

    public static <T> DataConvert<List<T>> newJsonArrayConvert(final JsonParser<T> parser) {
        return new DataConvert<List<T>>() {
            @Override
            public String asString(List<T> data) {
                JSONArray array = ParserUtil.toJson(data, parser);
                return array == null ? null : array.toString();
            }

            @Override
            public List<T> asData(String data) {
                if (TextUtils.isEmpty(data)) return null;
                try {
                    return ParserUtil.parseList(new JSONArray(data), parser);
                } catch (JSONException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
    }

    @EarlCall
    public String read(@NonNull String key) {
        if (MessageLoop.current() != mDbLoop) {
            throw new IllegalAccessError(
                    "This method must access in db thread, current is:" + Thread.currentThread());
        }
        if (mInnerDb == null) mInnerDb = new InnerDb(App.getAppContext());
        return mInnerDb.getValue(key);
    }

    public Task readAsync(@NonNull String key,
                          @NonNull Closure2_0<?, ?, String, RuntimeException> callback) {
        Task task = new RarTask(bind(read, this, key), callback);
        mDbLoop.postTask(task);
        return task;
    }

    @EarlCall
    public <T> T readData(@NonNull String key, @NonNull DataConvert<T> convert) {
        String str = read(key);
        if (str == null) return null;
        return convert.asData(str);
    }

    @SuppressWarnings("unchecked")
    public <T> Task readDataAsync(@NonNull String key, @NonNull DataConvert<T> convert,
                                  @NonNull Closure2_0<?, ?, T, RuntimeException> callback) {
        Task task = new RarTask(bind(readData, this, key, (DataConvert) convert), callback);
        mDbLoop.postTask(task);
        return task;
    }

    @EarlCall
    public boolean save(@NonNull String key, @Nullable String value) {
        if (MessageLoop.current() != mDbLoop) {
            throw new IllegalAccessError(
                    "This method must access in db thread, current is:" + Thread.currentThread());
        }
        if (mInnerDb == null) mInnerDb = new InnerDb(App.getAppContext());
        return mInnerDb.saveValue(key, value);
    }

    public Task saveAsync(@NonNull String key, @Nullable String value,
                          @Nullable Closure2_0<?, ?, Boolean, RuntimeException> callback) {
        Task task;
        if (callback == null) {
            task = bind(save, this, key, value).task();
        } else {
            task = new RarTask(bind(save, this, key, value), callback);
        }
        mDbLoop.postTask(task);
        return task;
    }

    @EarlCall
    public <T> boolean saveData(@NonNull String key, @NonNull DataConvert<T> convert,
                                @Nullable T data) {
        if (data == null) return save(key, null);
        return save(key, convert.asString(data));
    }

    @SuppressWarnings("unchecked")
    public <T> Task saveDataAsync(@NonNull String key, @NonNull DataConvert<T> convert,
                                  @Nullable T data, @Nullable Closure2_0<?, ?, Boolean, RuntimeException> callback) {
        Task task;
        if (callback == null) {
            task = bind(saveData, this, key, (DataConvert) convert, data).task();
        } else {
            task = new RarTask(bind(saveData, this, key, (DataConvert) convert, data), callback);
        }
        mDbLoop.postTask(task);
        return task;
    }

    @EarlCall
    public Set<String> readKeys() {
        if (MessageLoop.current() != mDbLoop) {
            throw new IllegalAccessError(
                    "This method must access in db thread, current is:" + Thread.currentThread());
        }
        if (mInnerDb == null) mInnerDb = new InnerDb(App.getAppContext());
        return mInnerDb.getKeys();
    }

    public Task readKeysAsync(@NonNull Closure2_0<?, ?, Set<String>, RuntimeException> callback) {
        Task task = new RarTask(bind(readKeys, this), callback);
        mDbLoop.postTask(task);
        return task;
    }

    @EarlCall
    public HashMap<String, String> readKeyValues() {
        if (MessageLoop.current() != mDbLoop) {
            throw new IllegalAccessError(
                    "This method must access in db thread, current is:" + Thread.currentThread());
        }
        if (mInnerDb == null) mInnerDb = new InnerDb(App.getAppContext());
        return mInnerDb.getKeyValues();
    }

    public Task readKeyValuesAsync(
            @NonNull Closure2_0<?, ?, HashMap<String, String>, RuntimeException> callback) {
        Task task = new RarTask(bind(readKeyValues, this), callback);
        mDbLoop.postTask(task);
        return task;
    }

    @DbThread
    private void covertOldData() {
        if (mFirstCreateListener == null) return;
        mFirstCreateListener.onStorageFirstCreate();
    }

    private static class InnerDb extends SQLiteOpenHelper {
        private static final String DB_NAME = "KvStorage";
        private static final int DB_VERSION = 1;
        //
        private static final String KV_TABLE = "KvTable";
        private static final String COL_KEY = "key";
        private static final String COL_VALUE = "value";
        private static final String[] COLS = new String[]{COL_VALUE};
        private static final String[] KEY_COLS = new String[]{COL_KEY};
        private ContentValues mTempCv = new ContentValues();
        private SQLiteDatabase mDbForCreating;

        InnerDb(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String sql = String.format(Locale.CHINA, "CREATE TABLE %s " +
                                                     "(%s INTEGER PRIMARY KEY, %s TEXT, %s TEXT)",
                                       KV_TABLE, BaseColumns._ID, COL_KEY, COL_VALUE);
            L.v(C.FACE, "%s~ onCreate:%s", TAG, sql);
            db.execSQL(sql);
            mDbForCreating = db;
            get().covertOldData();
            mDbForCreating = null;
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }

        @Override
        public SQLiteDatabase getReadableDatabase() {
            return mDbForCreating != null ? mDbForCreating : super.getReadableDatabase();
        }

        @Override
        public SQLiteDatabase getWritableDatabase() {
            return mDbForCreating != null ? mDbForCreating : super.getWritableDatabase();
        }

        String getValue(String key) {
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db
                    .query(KV_TABLE, COLS, COL_KEY + "=?", new String[]{key}, null, null, null);
            if (cursor.moveToNext()) {
                String rst = cursor.getString(0);
                cursor.close();
                if (L.loggable(C.FACE, L.DEBUG)) {
                    L.v(C.FACE, "%s~ getValue, key:%s, result:%s", TAG, key, rst);
                }
                return rst;
            }
            cursor.close();
            if (L.loggable(C.FACE, L.DEBUG)) {
                L.v(C.FACE, "%s~ getValue, key:%s, result:%s", TAG, key, null);
            }
            return null;
        }

        boolean saveValue(String key, String value) {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues cv = mTempCv;
            cv.clear();
            cv.put(COL_VALUE, value);
            int update = db.update(KV_TABLE, cv, COL_KEY + "=?", new String[]{key});
            // We need add new line.
            if (update <= 0) {
                cv.put(COL_KEY, key);
                if (L.loggable(C.FACE, L.DEBUG)) {
                    L.v(C.FACE, "%s~ saveValue-insert, key:%s, value:%s", TAG, key, value);
                }
                return db.insert(KV_TABLE, null, cv) > 0;
            }
            if (L.loggable(C.FACE, L.DEBUG)) {
                L.v(C.FACE, "%s~ saveValue-update, key:%s, value:%s", TAG, key, value);
            }
            return true;
        }

        Set<String> getKeys() {
            SQLiteDatabase db = getReadableDatabase();
            HashSet<String> keys = new HashSet<>();
            Cursor cursor = db.query(KV_TABLE, KEY_COLS, null, null, null, null, null);
            while (cursor.moveToNext()) {
                String rst = cursor.getString(0);
                keys.add(rst);
            }
            cursor.close();
            if (L.loggable(C.FACE, L.DEBUG)) {
                L.v(C.FACE, "%s~ getKeys, result:%s", TAG, keys);
            }
            return keys;
        }

        HashMap<String, String> getKeyValues() {
            SQLiteDatabase db = getReadableDatabase();
            HashMap<String, String> result = new HashMap<>();
            Cursor cursor = db.query(KV_TABLE, null, null, null, null, null, null);
            while (cursor.moveToNext()) {
                result.put(cursor.getString(1), cursor.getString(2));
            }
            cursor.close();
            if (L.loggable(C.FACE, L.DEBUG)) {
                L.v(C.FACE, "%s~ getKeyValues, result:%s", TAG, result);
            }
            return result;
        }
    }

    public static final Method1_0<KvStorage, String, String> read = new Method1_0<KvStorage, String, String>(
            KvStorage.class, "read") {
        @Override
        public String run(KvStorage target, @NonNull Params1<String> params) {
            return target.read(params.p1);
        }
    };
    private static final Method2_0<KvStorage, Object, String, DataConvert<Object>> readData = new Method2_0<KvStorage, Object, String, DataConvert<Object>>(
            KvStorage.class, "readData") {
        @Override
        public Object run(KvStorage target, @NonNull Params2<String, DataConvert<Object>> params) {
            return target.readData(params.p1, params.p2);
        }
    };
    public static final Method2_0<KvStorage, Boolean, String, String> save = new Method2_0<KvStorage, Boolean, String, String>(
            KvStorage.class, "save") {
        @Override
        public Boolean run(KvStorage target, @NonNull Params2<String, String> params) {
            return target.save(params.p1, params.p2);
        }
    };
    private static final Method3_0<KvStorage, Boolean, String, DataConvert<Object>, Object> saveData = new Method3_0<KvStorage, Boolean, String, DataConvert<Object>, Object>(
            KvStorage.class, "saveData") {
        @Override
        public Boolean run(KvStorage target,
                           @NonNull Params3<String, DataConvert<Object>, Object> params) {
            return target.saveData(params.p1, params.p2, params.p3);
        }
    };
    public static final Method0_0<KvStorage, Set<String>> readKeys = new Method0_0<KvStorage, Set<String>>(
            KvStorage.class, "readKeys") {
        @Override
        public Set<String> run(KvStorage target, @NonNull Params0 params) {
            return target.readKeys();
        }
    };
    public static final Method0_0<KvStorage, HashMap<String, String>> readKeyValues = new Method0_0<KvStorage, HashMap<String, String>>(
            KvStorage.class, "readKeyValues") {
        @Override
        public HashMap<String, String> run(KvStorage target, @NonNull Params0 params) {
            return target.readKeyValues();
        }
    };
}
