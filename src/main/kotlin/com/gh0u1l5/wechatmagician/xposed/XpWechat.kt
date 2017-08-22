package com.gh0u1l5.wechatmagician.xposed

import android.content.Context
import android.content.res.XModuleResources
import android.util.SparseArray
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class XpWechat : IXposedHookZygoteInit, IXposedHookLoadPackage {
    private val _hooks = SparseArray<WechatRevokeHook>()
    private lateinit var _res: XModuleResources

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam?) {
        _res = XModuleResources.createInstance(startupParam?.modulePath, null)
    }

    override fun handleLoadPackage(param: XC_LoadPackage.LoadPackageParam) {
        val pkgName = param.packageName
        if (pkgName != "com.tencent.mm") return

        val activityThread = XposedHelpers.callStaticMethod(XposedHelpers.findClass("android.app.ActivityThread", null), "currentActivityThread")
        val context = XposedHelpers.callMethod(activityThread, "getSystemContext") as Context?
        val versionName = context?.packageManager?.getPackageInfo(pkgName, 0)?.versionName
        getHooks(pkgName, versionName!!, param.appInfo.uid)?.hook(param.classLoader)
    }

    private fun getHooks(pkgName: String, versionName: String, uid: Int): WechatRevokeHook? {
        if (_hooks.indexOfKey(uid) == -1) {
            val _ver = WechatVersion(pkgName, versionName)
            _hooks.put(uid, WechatRevokeHook(_ver, _res))
        }
        return _hooks[uid]
    }
}