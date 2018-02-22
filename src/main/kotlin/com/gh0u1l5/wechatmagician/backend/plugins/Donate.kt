package com.gh0u1l5.wechatmagician.backend.plugins

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.gh0u1l5.wechatmagician.backend.WechatPackage
import com.gh0u1l5.wechatmagician.backend.interfaces.IUriRouterHook

object Donate : IUriRouterHook {

    private val pkg = WechatPackage

    override fun onUriRouterReceive(activity: Activity, uri: Uri) {
        val segments = uri.pathSegments
        if (segments.firstOrNull() == "donate") {
            activity.startActivity(Intent(activity, pkg.RemittanceAdapter).apply {
                putExtra("scene", 1)
                putExtra("pay_channel", 12)
                putExtra("receiver_name", "wxp://${segments[1]}")
            })
        }
    }
}