package com.gh0u1l5.wechatmagician.xposed

import android.content.res.XModuleResources
import android.util.SparseArray
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.callbacks.XC_LoadPackage

class XpWechat : IXposedHookZygoteInit, IXposedHookLoadPackage {

    private val hooks = SparseArray<WechatRevokeHook>()

    private fun getHook(uid: Int): WechatRevokeHook? {
        if (hooks.indexOfKey(uid) == -1) {
            hooks.put(uid, WechatRevokeHook(ver!!, res!!))
        }
        return hooks[uid]
    }

    companion object {
        var ver: WechatVersion? = null
        var res: XModuleResources? = null
        fun setVersion(ver: WechatVersion) { this.ver = this.ver ?: ver }
        fun setResource(res: XModuleResources) { this.res = this.res ?: res }
    }

    override fun initZygote(param: IXposedHookZygoteInit.StartupParam?) {
        setResource(XModuleResources.createInstance(param?.modulePath, null))
    }

    override fun handleLoadPackage(param: XC_LoadPackage.LoadPackageParam) {
        if (param.packageName == "com.tencent.mm") {
            setVersion(WechatVersion(param))
            getHook(param.appInfo.uid)?.hook(param.classLoader)
        }
    }
}