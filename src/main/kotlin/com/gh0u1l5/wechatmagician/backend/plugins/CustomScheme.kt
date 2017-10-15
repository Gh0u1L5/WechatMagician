package com.gh0u1l5.wechatmagician.backend.plugins

import android.app.Activity
import android.content.Intent
import com.gh0u1l5.wechatmagician.C
import com.gh0u1l5.wechatmagician.backend.WechatPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers.findAndHookMethod

class CustomScheme {

    val pkg = WechatPackage

    fun registerCustomSchemes() {
        if (pkg.WXCustomSchemeEntry == null || pkg.WXCustomSchemeEntryStart == "") {
            return
        }

        findAndHookMethod(
                pkg.WXCustomSchemeEntry, pkg.WXCustomSchemeEntryStart,
                C.Intent, object : XC_MethodHook() {
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

        pkg.status.CustomScheme = true
    }
}