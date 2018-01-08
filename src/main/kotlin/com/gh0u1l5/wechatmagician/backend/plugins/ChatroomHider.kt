package com.gh0u1l5.wechatmagician.backend.plugins

import android.widget.BaseAdapter
import com.gh0u1l5.wechatmagician.Global.SETTINGS_CHATTING_CHATROOM_HIDER
import com.gh0u1l5.wechatmagician.backend.WechatPackage
import com.gh0u1l5.wechatmagician.frontend.wechat.AdapterHider
import com.gh0u1l5.wechatmagician.storage.LocalizedStrings
import com.gh0u1l5.wechatmagician.storage.Preferences
import com.gh0u1l5.wechatmagician.storage.list.ChatroomHideList
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers.getObjectField

object ChatroomHider {

    private var preferences: Preferences? = null

    @JvmStatic fun init(_preferences: Preferences) {
        preferences = _preferences
    }

    private val str = LocalizedStrings
    private val pkg = WechatPackage

    fun changeChatroomStatus(username: String?, hide: Boolean) {
        if (username == null) {
            return
        }
        if (hide) {
            ChatroomHideList += username
        } else {
            ChatroomHideList -= username
        }
        pkg.ConversationAdapterObject.get()?.notifyDataSetChanged()
    }

    fun onAdapterCreated(param: XC_MethodHook.MethodHookParam) {
        if (!preferences!!.getBoolean(SETTINGS_CHATTING_CHATROOM_HIDER, false)) {
            return
        }
        val adapter = param.thisObject as BaseAdapter
        AdapterHider.register(adapter, "Chatroom Hider", { item ->
            val username = getObjectField(item, "field_username")
            username in ChatroomHideList
        })
    }
}