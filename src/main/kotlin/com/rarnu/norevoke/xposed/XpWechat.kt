package com.rarnu.norevoke.xposed

import android.content.Context
import android.text.TextUtils
import android.util.SparseArray
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Created by rarnu on 1/11/17.
 */
class XpWechat : IXposedHookLoadPackage {


    private val _hooks = SparseArray<WechatRevokeHook>()
    private val _supportVersions = arrayOf("6.5.3", "6.5.4")

    @Throws(Throwable::class)
    override fun handleLoadPackage(param: XC_LoadPackage.LoadPackageParam) {
        val pkgName = param.packageName
        if (!(pkgName.contains("com.tencen") && pkgName.contains("mm"))) {
            return
        }
        try {
            val activityThread = XposedHelpers.callStaticMethod(XposedHelpers.findClass("android.app.ActivityThread", null), "currentActivityThread")
            val context = XposedHelpers.callMethod(activityThread, "getSystemContext") as Context?
            try {
                val versionName = context?.packageManager?.getPackageInfo(pkgName, 0)?.versionName
                val hook = getHooks(pkgName, versionName!!, param.appInfo.uid)
                hook?.hook(param.classLoader)
            } catch (t: Throwable) {

            }
        } catch (t: Throwable) {

        }
    }

    private fun getHooks(pkgName: String, versionName: String, uid: Int): WechatRevokeHook? {
        if (_hooks.indexOfKey(uid) != -1) {
            return _hooks[uid]
        }
        if (isSupported(versionName)) {
            _hooks.put(uid, WechatRevokeHook(WechatVersion(pkgName, versionName)))
            return _hooks[uid]
        }
        return null
    }

    private fun isSupported(versionName: String): Boolean {
        if (TextUtils.isEmpty(versionName)) {
            return false
        }
        return _supportVersions.any { versionName.contains(it) }
    }
}