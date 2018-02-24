package com.gh0u1l5.wechatmagician.backend.foundation

import android.app.Activity
import android.content.Intent
import com.gh0u1l5.wechatmagician.Global.STATUS_FLAG_URI_ROUTER
import com.gh0u1l5.wechatmagician.backend.WechatPackage
import com.gh0u1l5.wechatmagician.backend.WechatPackage.WXCustomScheme
import com.gh0u1l5.wechatmagician.backend.WechatPackage.WXCustomSchemeEntryMethod
import com.gh0u1l5.wechatmagician.backend.foundation.base.EventCenter
import com.gh0u1l5.wechatmagician.backend.interfaces.IUriRouterHook
import com.gh0u1l5.wechatmagician.util.PackageUtil.findAndHookMethod
import de.robv.android.xposed.XC_MethodHook

object UriRouter : EventCenter() {
    @JvmStatic fun hookEvents() {
        findAndHookMethod(WXCustomScheme, WXCustomSchemeEntryMethod, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val intent = param.args[0] as Intent?
                val uri = intent?.data ?: return
                val activity = param.thisObject as Activity
                if (uri.host == "magician") {
                    notify("onUriRouterReceive") { plugin ->
                        if (plugin is IUriRouterHook) {
                            plugin.onUriRouterReceive(activity, uri)
                        }
                    }
                    param.result = false
                }
            }
        })

        WechatPackage.setStatus(STATUS_FLAG_URI_ROUTER, true)
    }
}