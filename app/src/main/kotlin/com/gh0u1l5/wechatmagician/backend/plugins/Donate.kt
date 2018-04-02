package com.gh0u1l5.wechatmagician.backend.plugins

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.gh0u1l5.wechatmagician.spellbook.interfaces.IUriRouterHook
import com.gh0u1l5.wechatmagician.spellbook.mirror.com.tencent.mm.plugin.remittance.ui.Classes.RemittanceAdapter

object Donate : IUriRouterHook {
    override fun onReceiveUri(activity: Activity, uri: Uri) {
        val segments = uri.pathSegments
        if (segments.firstOrNull() == "donate") {
            activity.startActivity(Intent(activity, RemittanceAdapter).apply {
                putExtra("scene", 1)
                putExtra("pay_channel", 12)
                putExtra("receiver_name", "wxp://${segments[1]}")
            })
        }
    }
}