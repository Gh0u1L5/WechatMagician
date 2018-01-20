package com.gh0u1l5.wechatmagician.backend

import android.content.res.XModuleResources
import com.gh0u1l5.wechatmagician.Global.STATUS_FLAG_RESOURCES
import com.gh0u1l5.wechatmagician.Global.WECHAT_PACKAGE_NAME
import de.robv.android.xposed.IXposedHookInitPackageResources
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.callbacks.XC_InitPackageResources

class WechatResHook : IXposedHookZygoteInit, IXposedHookInitPackageResources {

    companion object {
        @Volatile private var MODULE_PATH: String? = null
        @Volatile var MODULE_RES: XModuleResources? = null
    }

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        MODULE_PATH = startupParam.modulePath
    }

    override fun handleInitPackageResources(resparam: XC_InitPackageResources.InitPackageResourcesParam) {
        if (resparam.packageName != WECHAT_PACKAGE_NAME) {
            return
        }
        try {
            if (MODULE_RES == null) {
                MODULE_RES = XModuleResources.createInstance(MODULE_PATH, resparam.res)
                WechatPackage.setStatus(STATUS_FLAG_RESOURCES, true)
            }
        } catch (t: Throwable) {
            MODULE_RES = null; log(t)
        }
    }
}