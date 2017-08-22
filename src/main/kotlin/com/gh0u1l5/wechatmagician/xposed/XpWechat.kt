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

    companion object {
        var _ver: WechatVersion? = null
        var _res: XModuleResources? = null
        fun setVersion(ver: WechatVersion) { _ver = _ver ?: ver }
        fun setResource(res: XModuleResources) { _res = _res ?: res }
    }

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam?) {
        setResource(XModuleResources.createInstance(startupParam?.modulePath, null))
    }

    override fun handleLoadPackage(param: XC_LoadPackage.LoadPackageParam) {
        val pkgName = param.packageName
        if (pkgName != "com.tencent.mm") return

        val activityThread = XposedHelpers.findClass("android.app.ActivityThread", null)
        val currentThread = XposedHelpers.callStaticMethod(activityThread, "currentActivityThread")
        val systemContext = XposedHelpers.callMethod(currentThread, "getSystemContext") as Context
        val versionName = systemContext.packageManager.getPackageInfo(pkgName, 0).versionName
        setVersion(WechatVersion(pkgName, versionName))
        getHooks(param.appInfo.uid)?.hook(param.classLoader)
    }

    private fun getHooks(uid: Int): WechatRevokeHook? {
        if (_hooks.indexOfKey(uid) == -1) {
            _hooks.put(uid, WechatRevokeHook(_ver!!, _res!!))
        }
        return _hooks[uid]
    }
}