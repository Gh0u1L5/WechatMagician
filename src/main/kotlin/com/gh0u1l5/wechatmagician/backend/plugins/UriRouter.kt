package com.gh0u1l5.wechatmagician.backend.plugins

import android.app.Activity
import android.content.Intent
import com.gh0u1l5.wechatmagician.Global.STATUS_FLAG_URI_ROUTER
import com.gh0u1l5.wechatmagician.backend.WechatPackage
import com.gh0u1l5.wechatmagician.util.PackageUtil.findAndHookMethod
import de.robv.android.xposed.XC_MethodHook

object UriRouter {

    val pkg = WechatPackage

    @JvmStatic fun hijackUriRouter() {
        if (pkg.WXCustomScheme == null || pkg.WXCustomSchemeEntryMethod == null) {
            return
        }

        findAndHookMethod(pkg.WXCustomScheme, pkg.WXCustomSchemeEntryMethod, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                val intent = param.args[0] as Intent?
                val uri = intent?.data ?: return
                if (uri.host == "magician") {
                    val segments = uri.pathSegments
                    if (segments.isEmpty()) {
                        return
                    }
                    val activity = param.thisObject as Activity
                    when (segments[0]) {
                        "donate" -> {
                            if (pkg.RemittanceAdapter == null) {
                                return
                            }
                            activity.startActivity(Intent(activity, pkg.RemittanceAdapter).apply {
                                putExtra("scene", 1)
                                putExtra("pay_channel", 12)
                                putExtra("receiver_name", "wxp://${segments[1]}")
                            })
                        }
                    }
                    param.result = false
                }
            }
        })

        pkg.setStatus(STATUS_FLAG_URI_ROUTER, true)
    }
}