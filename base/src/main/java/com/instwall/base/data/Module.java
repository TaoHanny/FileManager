package com.instwall.base.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Player module, for init modules in order.
 * Eg: module A count on module B init finish. You can do this in module A's start()
 * initAfter(moduleB);
 * More modules can add as initAfter params.
 * <p>
 * This class is NOT threadsafe
 */
public abstract class Module {
    private boolean mInited;
    private List<Module> mOtherNeedInitAfterThis;
    private List<Module> mNeedWaitModules;

    public abstract void start();

    protected abstract void init();

    protected final void throwIfNotInit() {
        if (!mInited) throw new IllegalAccessError(
                "This method must called after module[" + this + "] inited!!!");
    }

    protected void initAfter(Module... others) {
        if (others == null) return;
        for (Module module : others) {
            if (module.mInited) continue;
            if (module.mOtherNeedInitAfterThis == null) {
                module.mOtherNeedInitAfterThis = new ArrayList<>();
            }
            module.mOtherNeedInitAfterThis.add(this);
            if (mNeedWaitModules == null) mNeedWaitModules = new ArrayList<>();
            mNeedWaitModules.add(module);
        }
        if (mNeedWaitModules == null) init();
    }

    protected void markInited() {
        if (mInited) return;
        mInited = true;
        if (mOtherNeedInitAfterThis == null) {
            return;
        }
        for (Module module : mOtherNeedInitAfterThis) {
            module.mNeedWaitModules.remove(this);
            if (module.mNeedWaitModules.isEmpty()) {
                module.mNeedWaitModules = null;
                module.init();
            }
        }
        mOtherNeedInitAfterThis.clear();
    }

    public boolean isInited() {
        return mInited;
    }
}
