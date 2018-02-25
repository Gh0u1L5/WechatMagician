package com.gh0u1l5.wechatmagician.backend.plugins

import android.app.Activity
import android.content.Context
import android.widget.BaseAdapter
import android.widget.Toast
import com.gh0u1l5.wechatmagician.Global.ITEM_ID_BUTTON_HIDE_FRIEND
import com.gh0u1l5.wechatmagician.Global.SETTINGS_SECRET_FRIEND
import com.gh0u1l5.wechatmagician.Global.SETTINGS_SECRET_FRIEND_PASSWORD
import com.gh0u1l5.wechatmagician.backend.WechatHook
import com.gh0u1l5.wechatmagician.backend.storage.LocalizedStrings
import com.gh0u1l5.wechatmagician.backend.storage.LocalizedStrings.BUTTON_HIDE_FRIEND
import com.gh0u1l5.wechatmagician.backend.storage.LocalizedStrings.PROMPT_SET_PASSWORD
import com.gh0u1l5.wechatmagician.backend.storage.LocalizedStrings.PROMPT_USER_NOT_FOUND
import com.gh0u1l5.wechatmagician.backend.storage.LocalizedStrings.PROMPT_VERIFY_PASSWORD
import com.gh0u1l5.wechatmagician.backend.storage.LocalizedStrings.TITLE_SECRET_FRIEND
import com.gh0u1l5.wechatmagician.backend.storage.database.MainDatabase.getContactByNickname
import com.gh0u1l5.wechatmagician.backend.storage.list.SecretFriendList
import com.gh0u1l5.wechatmagician.spellbook.WechatPackage.AddressAdapterObject
import com.gh0u1l5.wechatmagician.spellbook.WechatPackage.ConversationAdapterObject
import com.gh0u1l5.wechatmagician.spellbook.hookers.ListViewHider
import com.gh0u1l5.wechatmagician.spellbook.hookers.MenuAppender
import com.gh0u1l5.wechatmagician.spellbook.interfaces.*
import com.gh0u1l5.wechatmagician.util.PasswordUtil
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers.getObjectField

object SecretFriend : IActivityHook, IAdapterHook, INotificationHook, IPopupMenuHook, ISearchBarConsole {

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

    // Hide the message notifications from secret friends.
    override fun onMessageNotificationAdding(param: XC_MethodHook.MethodHookParam) {
        if (!isPluginEnabled()) {
            return
        }
        val notification = param.args[0].toString()
        val username = notification.substringAfter("userName: ").substringBefore(",")
        if (username in SecretFriendList) {
            param.result = null
        }
    }

    // Add menu items in the popup menu for contacts.
    // TODO: change the title of the button for hiding friends.
    override fun onPopupMenuForContactsCreating(username: String): MenuAppender.PopupMenuItem? {
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