package com.instwall.base.data;

import android.util.SparseArray;

/**
 * Created by phcy on 17-8-24.
 * 工具类　管理tracker id
 */

public class zza {
    private static final Object mLockObj = new Object();
    private static int zzbxt = 0;

    private SparseArray<Integer> zzbxu = new SparseArray<>();
    private SparseArray<Integer> zzbxv = new SparseArray<>();

    public zza() {
    }

    public int zzlf(int var1) {
        synchronized (mLockObj) {
            Integer var3 = this.zzbxu.get(var1);
            if (var3 != null) {
                return var3.intValue();
            } else {
                int var4 = zzbxt++;
                this.zzbxu.append(var1, Integer.valueOf(var4));
                this.zzbxv.append(var4, Integer.valueOf(var1));
                return var4;
            }
        }
    }

    public int zzlg(int var1) {
        synchronized (mLockObj) {
            return (this.zzbxv.get(var1)).intValue();
        }
    }
}