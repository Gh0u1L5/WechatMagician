package com.gh0u1l5.wechatmagician.backend

import android.os.Build
import com.gh0u1l5.wechatmagician.storage.LocalizedResources
import de.robv.android.xposed.IXposedHookInitPackageResources
import de.robv.android.xposed.callbacks.XC_InitPackageResources

class WechatResHook : IXposedHookInitPackageResources {
    override fun handleInitPackageResources(resparam: XC_InitPackageResources.InitPackageResourcesParam) {
        if (resparam.packageName != "com.tencent.mm") {
            return
        }

        LocalizedResources.language = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            resparam.res.configuration.locales[0]
        } else {
            resparam.res.configuration.locale
        }.language
    }
}