package com.gh0u1l5.wechatmagician.backend.plugins

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.widget.BaseAdapter
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

object SecretFriend {

    private var preferences: Preferences? = null

    @JvmStatic fun init(_preferences: Preferences) {
        preferences = _preferences
    }

    private val str = LocalizedStrings
    private val pkg = WechatPackage

    // These are the cache of adapter objects
    @Volatile var addressAdapter: Any? = null
    @Volatile var conversationAdapter: Any? = null

    fun changeUserStatus(context: Context, nickname: String, isSecret: Boolean) {
        val pref = context.getSharedPreferences(PREFERENCE_NAME_SECRET_FRIEND, MODE_PRIVATE)
        val username = getUsernameFromNickname(nickname)
        if (username == null) {
            Toast.makeText(
                    context, str[PROMPT_USER_NOT_FOUND], Toast.LENGTH_SHORT
            ).show()
        }
        if (isSecret) {
            SecretFriendList += username
        } else {
            SecretFriendList -= username
        }
        pref.edit().putBoolean(username, isSecret).apply()
        AdapterHider.notifyAdapter(addressAdapter)
        AdapterHider.notifyAdapter(conversationAdapter)
    }

    private fun hideItemView(param: XC_MethodHook.MethodHookParam) {
        if (!preferences!!.getBoolean(SETTINGS_SECRET_FRIEND, false)) {
            return
        }

        val adapter = param.thisObject as BaseAdapter
        val index = param.args[0] as Int
        param.args[0] = AdapterHider.onGetView(adapter, index)
    }

    private fun updateHideCache(param: XC_MethodHook.MethodHookParam) {
        if (!preferences!!.getBoolean(SETTINGS_SECRET_FRIEND, false)) {
            return
        }

        val adapter = param.thisObject as BaseAdapter
        AdapterHider.onDataSetChanged(adapter) { item ->
            val username = getObjectField(item, "field_username")
            username in SecretFriendList
        }
    }

    @JvmStatic fun tamperAdapterCount() {
        if (pkg.MMBaseAdapter == null) {
            return
        }

        findAndHookMethod(pkg.MMBaseAdapter, "getCount", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!preferences!!.getBoolean(SETTINGS_SECRET_FRIEND, false)) {
                    return
                }
                val adapter = param.thisObject as BaseAdapter
                val count = param.result as Int
                param.result = AdapterHider.onGetCount(adapter, count)
            }
        })
    }

    @JvmStatic fun hideSecretFriend() {
        if (pkg.AddressAdapter == null) {
            return
        }

        findAndHookMethod(
                pkg.AddressAdapter, "getView",
                C.Int, C.View, C.ViewGroup, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                addressAdapter = param.thisObject
                hideItemView(param)
            }
        })

        findAndHookMethod(pkg.AddressAdapter, "notifyDataSetChanged", object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) = updateHideCache(param)
        })
    }

    @JvmStatic fun hideSecretFriendConversation() {
        if (pkg.ConversationWithCacheAdapter == null) {
            return
        }

        findAndHookMethod(
                pkg.ConversationWithCacheAdapter, "getView",
                C.Int, C.View, C.ViewGroup, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                conversationAdapter = param.thisObject
                hideItemView(param)
            }
        })

        findAndHookMethod(pkg.ConversationWithCacheAdapter, "notifyDataSetChanged", object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) = updateHideCache(param)
        })
    }

}