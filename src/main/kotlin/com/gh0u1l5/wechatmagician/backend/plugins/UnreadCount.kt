package com.gh0u1l5.wechatmagician.backend.plugins

import android.widget.BaseAdapter
import com.gh0u1l5.wechatmagician.C
import com.gh0u1l5.wechatmagician.backend.WechatPackage
import com.gh0u1l5.wechatmagician.storage.Preferences
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import de.robv.android.xposed.XposedHelpers.setIntField

object UnreadCount {

    private var preferences: Preferences? = null

    @JvmStatic fun init(_preferences: Preferences) {
        preferences = _preferences
    }

    private val pkg = WechatPackage

    @JvmStatic fun disableMessageUnreadCount() {
        if (pkg.MMBaseAdapter == null || pkg.ConversationWithCacheAdapter == null) {
            return
        }

        findAndHookMethod(
                pkg.MMBaseAdapter, "getItem",
                C.Int, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!preferences!!.getBoolean("settings_disable_unread_count", false)) {
                    return
                }

                val adapter = param.thisObject as BaseAdapter
                if (adapter.javaClass == pkg.ConversationWithCacheAdapter) {
                    val item = param.result
                    setIntField(item, "field_unReadCount", 0)
                }
            }
        })
    }
}