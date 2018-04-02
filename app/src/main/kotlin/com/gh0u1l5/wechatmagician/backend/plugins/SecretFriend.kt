package com.gh0u1l5.wechatmagician.backend.plugins

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.BaseAdapter
import android.widget.Toast
import com.gh0u1l5.wechatmagician.Global.ITEM_ID_BUTTON_HIDE_FRIEND
import com.gh0u1l5.wechatmagician.Global.SETTINGS_SECRET_FRIEND
import com.gh0u1l5.wechatmagician.Global.SETTINGS_SECRET_FRIEND_HIDE_OPTION
import com.gh0u1l5.wechatmagician.Global.SETTINGS_SECRET_FRIEND_PASSWORD
import com.gh0u1l5.wechatmagician.R
import com.gh0u1l5.wechatmagician.backend.WechatHook
import com.gh0u1l5.wechatmagician.backend.storage.Strings
import com.gh0u1l5.wechatmagician.backend.storage.database.MainDatabase.getContactByNickname
import com.gh0u1l5.wechatmagician.backend.storage.list.SecretFriendList
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.AddressAdapterObject
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.ConversationAdapterObject
import com.gh0u1l5.wechatmagician.spellbook.hookers.ListViewHider
import com.gh0u1l5.wechatmagician.spellbook.hookers.MenuAppender
import com.gh0u1l5.wechatmagician.spellbook.interfaces.*
import com.gh0u1l5.wechatmagician.spellbook.mirror.com.tencent.mm.ui.chatting.Classes.ChattingUI
import com.gh0u1l5.wechatmagician.util.PasswordUtil
import de.robv.android.xposed.XposedHelpers.getObjectField

object SecretFriend : IActivityHook, IAdapterHook, INotificationHook, IPopupMenuHook, ISearchBarConsole {

    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }

    private val pref = WechatHook.settings

    private fun isPluginEnabled() = pref.getBoolean(SETTINGS_SECRET_FRIEND, true)

    private fun changeUserStatusByUsername(context: Context, username: String?, isSecret: Boolean) {
        if (username == null) {
            val promptUserNotFound = Strings.getString(R.string.prompt_user_not_found)
            Toast.makeText(context, promptUserNotFound, Toast.LENGTH_SHORT).show()
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
    override fun onActivityStarting(activity: Activity) {
        if (!isPluginEnabled()) {
            return
        }
        if (activity::class.java == ChattingUI) {
            val username = activity.intent.getStringExtra("Chat_User")
            if (username in SecretFriendList) {
                val promptUserNotFound = Strings.getString(R.string.prompt_user_not_found)
                Toast.makeText(activity, promptUserNotFound, Toast.LENGTH_SHORT).show()
                activity.finish()
            }
        }
    }

    override fun onMessageHandling(message: INotificationHook.Message): Boolean {
        if (!isPluginEnabled()) {
            return false
        }
        return message.talker in SecretFriendList
    }

    // Add menu items in the popup menu for contacts.
    override fun onPopupMenuForContactsCreating(username: String): List<MenuAppender.PopupMenuItem>? {
        if (!isPluginEnabled()) {
            return null
        }
        val textHideFriend = Strings.getString(R.string.button_hide_friend)
        val itemId = ITEM_ID_BUTTON_HIDE_FRIEND
        val title = pref.getString(SETTINGS_SECRET_FRIEND_HIDE_OPTION, textHideFriend)
        val onClickListener = { context: Context ->
            changeUserStatusByUsername(context, username, true)
        }
        return listOf(MenuAppender.PopupMenuItem(0, itemId, 0, title, onClickListener))
    }

    // Handle SearchBar commands to operate on secret friends.
    override fun onHandleCommand(context: Context, command: String): Boolean {
        if (!isPluginEnabled()) {
            return false
        }

        val titleSecretFriend = Strings.getString(R.string.title_secret_friend)
        val promptPasswordMissing = Strings.getString(R.string.prompt_password_missing)
        val promptVerifyPassword = Strings.getString(R.string.prompt_verify_password)

        when {
            command.startsWith("hide ") -> {
                val encrypted = pref.getString(SETTINGS_SECRET_FRIEND_PASSWORD, "")
                if (encrypted.isEmpty()) {
                    mainHandler.post {
                        Toast.makeText(context, promptPasswordMissing, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val nickname = command.drop("hide ".length)
                    changeUserStatusByNickname(context, nickname, true)
                }
                return true
            }
            command.startsWith("unhide ") -> {
                val encrypted = pref.getString(SETTINGS_SECRET_FRIEND_PASSWORD, "")
                if (encrypted.isEmpty()) {
                    mainHandler.post {
                        Toast.makeText(context, promptPasswordMissing, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    mainHandler.post {
                        PasswordUtil.askPasswordWithVerify(context, titleSecretFriend, promptVerifyPassword, encrypted) {
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
                }
                return true
            }
        }
        return super.onHandleCommand(context, command)
    }
}