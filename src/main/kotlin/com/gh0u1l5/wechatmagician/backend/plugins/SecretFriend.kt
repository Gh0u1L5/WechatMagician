package com.gh0u1l5.wechatmagician.backend.plugins

import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.view.ContextMenu
import android.view.View
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.Toast
import com.gh0u1l5.wechatmagician.C
import com.gh0u1l5.wechatmagician.Global.ITEM_ID_BUTTON_HIDE_FRIEND
import com.gh0u1l5.wechatmagician.Global.PREFERENCE_NAME_SECRET_FRIEND
import com.gh0u1l5.wechatmagician.Global.SETTINGS_SECRET_FRIEND
import com.gh0u1l5.wechatmagician.backend.WechatPackage
import com.gh0u1l5.wechatmagician.frontend.wechat.AdapterHider
import com.gh0u1l5.wechatmagician.storage.LocalizedStrings
import com.gh0u1l5.wechatmagician.storage.LocalizedStrings.BUTTON_HIDE_FRIEND
import com.gh0u1l5.wechatmagician.storage.LocalizedStrings.PROMPT_USER_NOT_FOUND
import com.gh0u1l5.wechatmagician.storage.Preferences
import com.gh0u1l5.wechatmagician.storage.database.MainDatabase.cacheNicknameUsernamePair
import com.gh0u1l5.wechatmagician.storage.database.MainDatabase.getUsernameFromNickname
import com.gh0u1l5.wechatmagician.storage.list.SecretFriendList
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers.*
import kotlin.concurrent.thread

object SecretFriend {

    private var loader: ClassLoader? = null
    private var preferences: Preferences? = null

    @JvmStatic fun init(_loader: ClassLoader, _preferences: Preferences) {
        loader = _loader
        preferences = _preferences
    }

    private val str = LocalizedStrings
    private val pkg = WechatPackage

    // These are the cache of adapter objects
    @Volatile var addressAdapter: Any? = null
    @Volatile var conversationAdapter: Any? = null

    @Volatile var currentUsername: String? = null

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
        AdapterHider.notifyAdapter(addressAdapter)
        AdapterHider.notifyAdapter(conversationAdapter)
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

    private fun recordCurrentUsername(param: XC_MethodHook.MethodHookParam) {
        if (!preferences!!.getBoolean(SETTINGS_SECRET_FRIEND, false)) {
            return
        }

        val parent = param.args[0] as AdapterView<*>
        val position = param.args[2] as Int

        val adapter = parent.adapter
        val item = adapter.getItem(position)
        currentUsername = getObjectField(item, "field_username") as String?
    }

    private fun addHideOption(param: XC_MethodHook.MethodHookParam) {
        if (!preferences!!.getBoolean(SETTINGS_SECRET_FRIEND, false)) {
            return
        }

        val menu = param.args[0] as ContextMenu
        val view = param.args[1] as View
        val item = menu.add(0, ITEM_ID_BUTTON_HIDE_FRIEND, 0, str[BUTTON_HIDE_FRIEND])
        item.setOnMenuItemClickListener {
            changeUserStatusByUsername(view.context, currentUsername, true)
            return@setOnMenuItemClickListener true
        }
    }

    @JvmStatic fun addHideOptionInPopupMenu() {
        findAndHookMethod(
                pkg.ContactLongClickListener, "onItemLongClick",
                C.AdapterView, C.View, C.Int, C.Long, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) = recordCurrentUsername(param)
        })

        findAndHookMethod(
                pkg.AddressUI, "onCreateContextMenu",
                C.ContextMenu, C.View, C.ContextMenuInfo, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) = addHideOption(param)
        })

        findAndHookMethod(
                pkg.ConversationLongClickListener, "onItemLongClick",
                C.AdapterView, C.View, C.Int, C.Long, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) = recordCurrentUsername(param)
        })

        findAndHookMethod(
                pkg.ConversationCreateContextMenuListener, "onCreateContextMenu",
                C.ContextMenu, C.View, C.ContextMenuInfo, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) = addHideOption(param)
        })

        findAndHookMethod(pkg.MMListPopupWindow, "show", object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (!preferences!!.getBoolean(SETTINGS_SECRET_FRIEND, false)) {
                    return
                }

                val listenerField = findFirstFieldByExactType(pkg.MMListPopupWindow, C.AdapterView_OnItemClickListener)
                val listener = listenerField.get(param.thisObject) as AdapterView.OnItemClickListener
                listenerField.set(param.thisObject, AdapterView.OnItemClickListener { parent, view, position, id ->
                    val item = parent.adapter.getItem(position)
                    when (item) {
                        str[BUTTON_HIDE_FRIEND] -> {
                            changeUserStatusByUsername(view.context, currentUsername, true)
                            callMethod(param.thisObject, "dismiss")
                        }
                        else ->
                            listener.onItemClick(parent, view, position, id)
                    }
                })
            }
        })
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
                    addressAdapter = param.thisObject
                    hideItem(param)
                }
            }
        })

        findAndHookMethod(pkg.AddressAdapter, "notifyDataSetChanged", object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                val adapter = param.thisObject as BaseAdapter
                thread(start = true) {
                    (0 until adapter.count).forEach { index ->
                        try {
                            val item = adapter.getItem(index)
                            val nickname = getObjectField(item, "field_nickname")
                            val username = getObjectField(item, "field_username")
                            cacheNicknameUsernamePair(nickname as? String, username as? String)
                        } catch (_: IndexOutOfBoundsException) {
                            // Ignore this one
                        }
                    }
                }
                updateHideCache(param)
            }
        })
    }

    @JvmStatic fun hideSecretFriendConversation() {
        findAndHookMethod(pkg.MMBaseAdapter, pkg.MMBaseAdapterGetMethod, C.Int, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (param.thisObject.javaClass == pkg.ConversationWithCacheAdapter) {
                    conversationAdapter = param.thisObject
                    hideItem(param)
                }
            }
        })

        findAndHookMethod("android.widget.BaseAdapter", loader, "notifyDataSetChanged", object : XC_MethodHook() {
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