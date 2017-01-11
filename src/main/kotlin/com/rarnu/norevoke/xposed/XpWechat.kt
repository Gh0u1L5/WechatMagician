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


    private val hooks = SparseArray<WechatRevokeHook>()
    private val supportVersions = arrayOf("6.5.3")

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
        if (hooks.indexOfKey(uid) != -1) {
            return hooks[uid]
        }
        if (isSupported(versionName)) {
            hooks.put(uid, WechatRevokeHook(WechatVersion(pkgName, versionName)))
            return hooks[uid]
        }
        return null
    }

    private fun isSupported(versionName: String): Boolean {
        if (TextUtils.isEmpty(versionName)) {
            return false
        }
        return supportVersions.any { versionName.contains(it) }
    }
}