package com.gh0u1l5.wechatmagician.backend.plugins

import android.app.Activity
import android.content.Context
import android.widget.BaseAdapter
import android.widget.Toast
import com.gh0u1l5.wechatmagician.Global.ITEM_ID_BUTTON_HIDE_FRIEND
import com.gh0u1l5.wechatmagician.Global.SETTINGS_SECRET_FRIEND
import com.gh0u1l5.wechatmagician.Global.SETTINGS_SECRET_FRIEND_PASSWORD
import com.gh0u1l5.wechatmagician.backend.WechatHook
import com.gh0u1l5.wechatmagician.backend.WechatPackage.AddressAdapterObject
import com.gh0u1l5.wechatmagician.backend.WechatPackage.ConversationAdapterObject
import com.gh0u1l5.wechatmagician.backend.foundation.ListViewHider
import com.gh0u1l5.wechatmagician.backend.foundation.MenuAppender
import com.gh0u1l5.wechatmagician.backend.interfaces.IActivityHook
import com.gh0u1l5.wechatmagician.backend.interfaces.IAdapterHook
import com.gh0u1l5.wechatmagician.backend.interfaces.IPopupMenuHook
import com.gh0u1l5.wechatmagician.backend.interfaces.ISearchBarConsole
import com.gh0u1l5.wechatmagician.storage.LocalizedStrings
import com.gh0u1l5.wechatmagician.storage.LocalizedStrings.BUTTON_HIDE_FRIEND
import com.gh0u1l5.wechatmagician.storage.LocalizedStrings.PROMPT_SET_PASSWORD
import com.gh0u1l5.wechatmagician.storage.LocalizedStrings.PROMPT_USER_NOT_FOUND
import com.gh0u1l5.wechatmagician.storage.LocalizedStrings.PROMPT_VERIFY_PASSWORD
import com.gh0u1l5.wechatmagician.storage.LocalizedStrings.TITLE_SECRET_FRIEND
import com.gh0u1l5.wechatmagician.storage.database.MainDatabase.getContactByNickname
import com.gh0u1l5.wechatmagician.storage.list.SecretFriendList
import com.gh0u1l5.wechatmagician.util.PasswordUtil
import de.robv.android.xposed.XposedHelpers.getObjectField

object SecretFriend : IActivityHook, IAdapterHook, IPopupMenuHook, ISearchBarConsole {

    private val str = LocalizedStrings
    private val pref = WechatHook.settings

    private fun isPluginEnabled() = pref.getBoolean(SETTINGS_SECRET_FRIEND, true)

    private fun changeUserStatusByUsername(context: Context, username: String?, isSecret: Boolean) {
        if (username == null) {
            Toast.makeText(
                    context, str[PROMPT_USER_NOT_FOUND], Toast.LENGTH_SHORT
            ).show()
            return
        }
        if (isSecret) {
            SecretFriendList += username
        } else {
            SecretFriendList -= username
        }
        AddressAdapterObject.get()?.notifyDataSetChanged()
        ConversationAdapterObject.get()?.notifyDataSetChanged()
    }

    private fun changeUserStatusByNickname(context: Context, nickname: String?, isSecret: Boolean) {
        if (nickname == null) {
            return
        }
        val username = getContactByNickname(nickname)?.username
        changeUserStatusByUsername(context, username, isSecret)
    }

    private fun onAdapterCreated(adapter: BaseAdapter) {
        if (!isPluginEnabled()) {
            return
        }
        ListViewHider.register(adapter, "Secret Friend", { item ->
            val username = getObjectField(item, "field_username")
            username in SecretFriendList
        })
    }

    override fun onAddressAdapterCreated(adapter: BaseAdapter) = onAdapterCreated(adapter)

    override fun onConversationAdapterCreated(adapter: BaseAdapter) = onAdapterCreated(adapter)

    // Hide the chatting windows for secret friends.
    override fun onChattingUICreated(activity: Activity) {
        if (!isPluginEnabled()) {
            return
        }
        val username = activity.intent.getStringExtra("Chat_User")
        if (username in SecretFriendList) {
            Toast.makeText(
                    activity, str[PROMPT_USER_NOT_FOUND], Toast.LENGTH_SHORT
            ).show()
            activity.finish()
        }
    }

    // TODO: add hideNotifications

    // Add menu items in the popup menu for contacts.
    // TODO: change the title of the button for hiding friends.
    override fun onCreatePopupMenuForContacts(username: String): MenuAppender.PopupMenuItem? {
        if (!isPluginEnabled()) {
            return null
        }
        val itemId = ITEM_ID_BUTTON_HIDE_FRIEND
        val title = str[BUTTON_HIDE_FRIEND]
        val onClickListener = { context: Context ->
            changeUserStatusByUsername(context, username, true)
        }
        return MenuAppender.PopupMenuItem(0, itemId, 0, title, onClickListener)
    }

    // Handle SearchBar commands to operate on secret friends.
    override fun onHandleCommand(context: Context, command: String): Boolean {
        if (!isPluginEnabled()) {
            return false
        }
        when {
            command.startsWith("hide ") -> {
                val encrypted = pref.getString(SETTINGS_SECRET_FRIEND_PASSWORD, "")
                if (encrypted == "") {
                    Toast.makeText(
                            context, str[PROMPT_SET_PASSWORD], Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val nickname = command.drop("hide ".length)
                    changeUserStatusByNickname(context, nickname, true)
                }
                return true
            }
            command.startsWith("unhide ") -> {
                val encrypted = pref.getString(SETTINGS_SECRET_FRIEND_PASSWORD, "")
                if (encrypted == "") {
                    Toast.makeText(
                            context, str[PROMPT_SET_PASSWORD], Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val title = str[TITLE_SECRET_FRIEND]
                    val message = str[PROMPT_VERIFY_PASSWORD]
                    PasswordUtil.askPasswordWithVerify(context, title, message, encrypted) {
                        val nickname = command.drop("unhide ".length)
                        if (nickname == "all") {
                            SecretFriendList.forEach { username ->
                                changeUserStatusByUsername(context, username, false)
                            }
                        } else {
                            changeUserStatusByNickname(context, nickname, false)
                        }
                    }
                }
                return true
            }
        }
        return super.onHandleCommand(context, command)
    }
}