package com.gh0u1l5.wechatmagician.backend.plugins

import android.app.Activity
import android.widget.Button
import com.gh0u1l5.wechatmagician.C
import com.gh0u1l5.wechatmagician.Global.SETTINGS_AUTO_LOGIN
import com.gh0u1l5.wechatmagician.backend.WechatHook
import com.gh0u1l5.wechatmagician.backend.interfaces.IActivityHook
import de.robv.android.xposed.XposedHelpers.findFirstFieldByExactType

object AutoLogin : IActivityHook {

    private val pref = WechatHook.settings

    private fun isPluginEnabled() = pref.getBoolean(SETTINGS_AUTO_LOGIN, false)

    override fun onWebLoginUICreated(activity: Activity) {
        if (!isPluginEnabled()) {
            return
        }
        val clazz = activity::class.java
        val field = findFirstFieldByExactType(clazz, C.Button)
        val button = field.get(activity) as Button?
        button?.performClick()
    }
}