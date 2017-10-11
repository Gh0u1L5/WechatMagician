package com.gh0u1l5.wechatmagician.backend

import android.content.res.XModuleResources
import android.os.Build
import com.gh0u1l5.wechatmagician.storage.HookStatus
import com.gh0u1l5.wechatmagician.storage.LocalizedResources
import de.robv.android.xposed.IXposedHookInitPackageResources
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.callbacks.XC_InitPackageResources

class WechatResHook : IXposedHookZygoteInit, IXposedHookInitPackageResources {

    companion object {
        private var MODULE_PATH: String? = null
        var MODULE_RES: XModuleResources? = null
    }

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        MODULE_PATH = startupParam.modulePath
    }

    override fun handleInitPackageResources(resparam: XC_InitPackageResources.InitPackageResourcesParam) {
        if (resparam.packageName != "com.tencent.mm") {
            return
        }

        // Set language for LocalizedResources
        LocalizedResources.language = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            resparam.res.configuration.locales[0]
        } else {
            resparam.res.configuration.locale
        }.language

        // Load resources
        MODULE_RES = XModuleResources.createInstance(MODULE_PATH, resparam.res)

        HookStatus += "Resources"
    }
}