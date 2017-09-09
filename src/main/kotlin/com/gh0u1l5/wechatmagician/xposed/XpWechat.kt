package com.gh0u1l5.wechatmagician.xposed

import android.content.res.XModuleResources
import android.util.SparseArray
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
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
        if (param.packageName == "com.tencent.mm") {
            setVersion(WechatVersion(param))
            getHooks(param.appInfo.uid)?.hook(param.classLoader)
        }
    }

    private fun getHooks(uid: Int): WechatRevokeHook? {
        if (_hooks.indexOfKey(uid) == -1) {
            _hooks.put(uid, WechatRevokeHook(_ver!!, _res!!))
        }
        return _hooks[uid]
    }
}