package com.rarnu.norevoke.xposed

import android.content.Context
import android.util.SparseArray
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class XpWechat : IXposedHookLoadPackage {

    private val _hooks = SparseArray<WechatRevokeHook>()

    @Throws(Throwable::class)
    override fun handleLoadPackage(param: XC_LoadPackage.LoadPackageParam) {
        val pkgName = param.packageName
        if (pkgName != "com.tencent.mm") return

        try {
            val activityThread = XposedHelpers.callStaticMethod(XposedHelpers.findClass("android.app.ActivityThread", null), "currentActivityThread")
            val context = XposedHelpers.callMethod(activityThread, "getSystemContext") as Context?
            val versionName = context?.packageManager?.getPackageInfo(pkgName, 0)?.versionName
            getHooks(pkgName, versionName!!, param.appInfo.uid)?.hook(param.classLoader)
        } catch (t: Throwable) { }
    }

    private fun getHooks(pkgName: String, versionName: String, uid: Int): WechatRevokeHook? {
        if (_hooks.indexOfKey(uid) == -1)
            _hooks.put(uid, WechatRevokeHook(WechatVersion(pkgName, versionName)))
        return _hooks[uid]
    }
}