package com.gh0u1l5.wechatmagician.backend.plugins

import android.app.Activity
import android.widget.Button
import com.gh0u1l5.wechatmagician.Global.SETTINGS_AUTO_LOGIN
import com.gh0u1l5.wechatmagician.backend.WechatHook
import com.gh0u1l5.wechatmagician.spellbook.interfaces.IActivityHook
import com.gh0u1l5.wechatmagician.spellbook.mirror.com.tencent.mm.plugin.webwx.ui.Classes.ExtDeviceWXLoginUI
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.XposedHelpers.findFirstFieldByExactType

object AutoLogin : IActivityHook {

    private val pref = WechatHook.settings

    private fun isPluginEnabled() = pref.getBoolean(SETTINGS_AUTO_LOGIN, false)

    override fun onActivityStarting(activity: Activity) {
        if (!isPluginEnabled()) {
            return
        }
        if (activity::class.java == ExtDeviceWXLoginUI) {
            val field = findFirstFieldByExactType(ExtDeviceWXLoginUI, Button::class.java)
            val button = field.get(activity) as Button?
            log("field = $field, button = $button")
            button?.performClick()
        }
    }
}