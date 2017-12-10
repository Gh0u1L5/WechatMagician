package com.gh0u1l5.wechatmagician.backend.plugins

import android.app.Activity
import android.content.Intent
import com.gh0u1l5.wechatmagician.backend.WechatPackage
import com.gh0u1l5.wechatmagician.storage.database.MainDatabase

object OneClick {

    private val pkg = WechatPackage

    fun cleanUnreadCount(activity: Activity?) {
        if (activity == null) {
            return
        }

        MainDatabase.cleanUnreadCount()
        activity.finish()
        activity.startActivity(Intent(activity, pkg.LauncherUI).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME)
        })
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}