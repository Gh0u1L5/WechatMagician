package com.gh0u1l5.wechatmagician.backend.plugins

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.view.ContextMenu
import android.view.View
import android.widget.AdapterView
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
import com.gh0u1l5.wechatmagician.storage.database.MainDatabase.getUsernameFromNickname
import com.gh0u1l5.wechatmagician.storage.list.SecretFriendList
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers.*

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

    private fun changeUserStatusByUsername(context: Context, username: String?, isSecret: Boolean) {
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

    private fun hideItemView(param: XC_MethodHook.MethodHookParam) {
        if (!preferences!!.getBoolean(SETTINGS_SECRET_FRIEND, false)) {
            return
        }
        AdapterHider.beforeGetView(param)
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

    @JvmStatic fun addHideOptionInPopupMenu() {
        when (null) {
            pkg.AddressUI,
            pkg.ContactLongClickListener,
            pkg.ConversationLongClickListener -> return
        }

        var currentUsername: String? = ""

        findAndHookMethod(
                pkg.ContactLongClickListener, "onItemLongClick",
                C.AdapterView, C.View, C.Int, C.Long, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (!preferences!!.getBoolean(SETTINGS_SECRET_FRIEND, false)) {
                    return
                }

                val parent = param.args[0] as AdapterView<*>
                val position = param.args[2] as Int

                val adapter = parent.adapter
                val item = adapter.getItem(position)
                currentUsername = getObjectField(item, "field_username") as String?
            }
        })

        findAndHookMethod(
                pkg.AddressUI, "onCreateContextMenu",
                C.ContextMenu, C.View, C.ContextMenuInfo, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!preferences!!.getBoolean(SETTINGS_SECRET_FRIEND, false)) {
                    return
                }

                val menu = param.args[0] as ContextMenu
                val view = param.args[1] as View? ?: return

                val item = menu.add(0, ITEM_ID_BUTTON_HIDE_FRIEND, 0, str[BUTTON_HIDE_FRIEND])
                item.setOnMenuItemClickListener { // TODO: check if this is needed
                    changeUserStatusByUsername(view.context, currentUsername, true)
                    return@setOnMenuItemClickListener true
                }
            }
        })

        findAndHookMethod(
                pkg.ConversationLongClickListener, "onItemLongClick",
                C.AdapterView, C.View, C.Int, C.Long, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (!preferences!!.getBoolean(SETTINGS_SECRET_FRIEND, false)) {
                    return
                }

                val parent = param.args[0] as AdapterView<*>
                val position = param.args[2] as Int

                val adapter = parent.adapter
                val item = adapter.getItem(position)
                currentUsername = getObjectField(item, "field_username") as String?
            }
        })

        findAndHookMethod(
                pkg.ConversationLongClickListener, "onCreateContextMenu",
                C.ContextMenu, C.View, C.ContextMenuInfo, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!preferences!!.getBoolean(SETTINGS_SECRET_FRIEND, false)) {
                    return
                }

                val menu = param.args[0] as ContextMenu
                val view = param.args[1] as View? ?: return

                val item = menu.add(0, ITEM_ID_BUTTON_HIDE_FRIEND, 0, str[BUTTON_HIDE_FRIEND])
                item.setOnMenuItemClickListener {
                    changeUserStatusByUsername(view.context, currentUsername, true)
                    return@setOnMenuItemClickListener true
                }
            }
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

    @JvmStatic fun tamperAdapterCount() {
        if (pkg.MMBaseAdapter == null) {
            return
        }

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