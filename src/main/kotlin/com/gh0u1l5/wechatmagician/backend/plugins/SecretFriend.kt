package com.gh0u1l5.wechatmagician.backend.plugins

import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.widget.Toast
import com.gh0u1l5.wechatmagician.C
import com.gh0u1l5.wechatmagician.Global.PREFERENCE_NAME_SECRET_FRIEND
import com.gh0u1l5.wechatmagician.Global.SETTINGS_SECRET_FRIEND
import com.gh0u1l5.wechatmagician.backend.WechatPackage
import com.gh0u1l5.wechatmagician.frontend.wechat.AdapterHider
import com.gh0u1l5.wechatmagician.storage.LocalizedStrings
import com.gh0u1l5.wechatmagician.storage.LocalizedStrings.PROMPT_USER_NOT_FOUND
import com.gh0u1l5.wechatmagician.storage.Preferences
import com.gh0u1l5.wechatmagician.storage.database.MainDatabase.getUsernameFromNickname
import com.gh0u1l5.wechatmagician.storage.list.SecretFriendList
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import de.robv.android.xposed.XposedHelpers.getObjectField
import java.lang.ref.WeakReference

object SecretFriend {

    private var preferences: Preferences? = null

    @JvmStatic fun init(_preferences: Preferences) {
        preferences = _preferences
    }

    private val str = LocalizedStrings
    private val pkg = WechatPackage

    // These are the cache of adapter objects
    @Volatile var addressAdapter: WeakReference<Any?> = WeakReference(null)
    @Volatile var conversationAdapter: WeakReference<Any?> = WeakReference(null)

    fun changeUserStatusByUsername(context: Context, username: String?, isSecret: Boolean) {
        if (username == null) {
            return
        }
        if (isSecret) {
            SecretFriendList += username
        } else {
            SecretFriendList -= username
        }
        val pref = context.getSharedPreferences(PREFERENCE_NAME_SECRET_FRIEND, MODE_PRIVATE)
        pref.edit().putBoolean(username, isSecret).apply()
        AdapterHider.notifyAdapter(addressAdapter.get())
        AdapterHider.notifyAdapter(conversationAdapter.get())
    }

    fun changeUserStatusByNickname(context: Context, nickname: String?, isSecret: Boolean) {
        if (nickname == null) {
            return
        }
        val username = getUsernameFromNickname(nickname)
        if (username == null) {
            Toast.makeText(
                    context, str[PROMPT_USER_NOT_FOUND], Toast.LENGTH_SHORT
            ).show()
            return
        }
        changeUserStatusByUsername(context, username, isSecret)
    }

    private fun hideItem(param: XC_MethodHook.MethodHookParam) {
        if (!preferences!!.getBoolean(SETTINGS_SECRET_FRIEND, false)) {
            return
        }
        AdapterHider.beforeGetItem(param)
    }

    private fun updateHideCache(param: XC_MethodHook.MethodHookParam) {
        if (!preferences!!.getBoolean(SETTINGS_SECRET_FRIEND, false)) {
            return
        }
        AdapterHider.beforeNotifyDataSetChanged(param) { item ->
            val username = getObjectField(item, "field_username")
            username in SecretFriendList
        }
    }

    @JvmStatic fun tamperAdapterCount() {
        findAndHookMethod(pkg.MMBaseAdapter, "getCount", object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!preferences!!.getBoolean(SETTINGS_SECRET_FRIEND, false)) {
                    return
                }
                AdapterHider.afterGetCount(param) }
        })
    }

    @JvmStatic fun hideSecretFriend() {
        findAndHookMethod(pkg.MMBaseAdapter, pkg.MMBaseAdapterGetMethod, C.Int, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (param.thisObject.javaClass == pkg.AddressAdapter) {
                    addressAdapter = WeakReference(param.thisObject)
                    hideItem(param)
                }
            }
        })

        findAndHookMethod(pkg.AddressAdapter, "notifyDataSetChanged", object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                updateHideCache(param)
            }
        })
    }

    @JvmStatic fun hideSecretFriendConversation() {
        findAndHookMethod(pkg.MMBaseAdapter, pkg.MMBaseAdapterGetMethod, C.Int, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (param.thisObject.javaClass == pkg.ConversationWithCacheAdapter) {
                    conversationAdapter = WeakReference(param.thisObject)
                    hideItem(param)
                }
            }
        })

        findAndHookMethod(pkg.BaseAdapter, "notifyDataSetChanged", object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (param.thisObject.javaClass == pkg.ConversationWithCacheAdapter) {
                    updateHideCache(param)
                }
            }
        })
    }

    @JvmStatic fun hideSecretFriendChattingWindow() {
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