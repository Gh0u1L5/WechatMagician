package com.gh0u1l5.wechatmagician.backend.plugins

import android.app.Activity
import android.content.Context
import android.widget.BaseAdapter
import android.widget.Toast
import com.gh0u1l5.wechatmagician.C
import com.gh0u1l5.wechatmagician.Global.SETTINGS_SECRET_FRIEND
import com.gh0u1l5.wechatmagician.backend.WechatPackage
import com.gh0u1l5.wechatmagician.frontend.wechat.AdapterHider
import com.gh0u1l5.wechatmagician.storage.LocalizedStrings
import com.gh0u1l5.wechatmagician.storage.LocalizedStrings.PROMPT_USER_NOT_FOUND
import com.gh0u1l5.wechatmagician.storage.Preferences
import com.gh0u1l5.wechatmagician.storage.database.MainDatabase.getContactByNickname
import com.gh0u1l5.wechatmagician.storage.list.SecretFriendList
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import de.robv.android.xposed.XposedHelpers.getObjectField

object SecretFriend {

    private var preferences: Preferences? = null

    @JvmStatic fun init(_preferences: Preferences) {
        preferences = _preferences
    }

    private val str = LocalizedStrings
    private val pkg = WechatPackage

    fun changeUserStatusByUsername(context: Context, username: String?, isSecret: Boolean) {
        if (username == null) {
            return
        }
        if (isSecret) {
            SecretFriendList += username
        } else {
            SecretFriendList -= username
        }
        pkg.AddressAdapterObject.get()?.notifyDataSetChanged()
        pkg.ConversationAdapterObject.get()?.notifyDataSetChanged()
    }

    fun changeUserStatusByNickname(context: Context, nickname: String?, isSecret: Boolean) {
        if (nickname == null) {
            return
        }
        val username = getContactByNickname(nickname)?.username
        if (username == null) {
            Toast.makeText(
                    context, str[PROMPT_USER_NOT_FOUND], Toast.LENGTH_SHORT
            ).show()
            return
        }
        changeUserStatusByUsername(context, username, isSecret)
    }

    fun onAdapterCreated(param: XC_MethodHook.MethodHookParam) {
        if (!preferences!!.getBoolean(SETTINGS_SECRET_FRIEND, false)) {
            return
        }
        val adapter = param.thisObject as BaseAdapter
        AdapterHider.register(adapter, "Secret Friend", { item ->
            val username = getObjectField(item, "field_username")
            username in SecretFriendList
        })
    }

    @JvmStatic fun hideChattingWindow() {
        findAndHookMethod(pkg.ChattingUI, "onCreate", C.Bundle, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (!preferences!!.getBoolean(SETTINGS_SECRET_FRIEND, false)) {
                    return
                }

                val activity = param.thisObject as Activity
                val username = activity.intent.getStringExtra("Chat_User")
                if (username in SecretFriendList) {
                    Toast.makeText(
                            activity, str[PROMPT_USER_NOT_FOUND], Toast.LENGTH_SHORT
                    ).show()
                    activity.finish()
                }
            }
        })
    }
}