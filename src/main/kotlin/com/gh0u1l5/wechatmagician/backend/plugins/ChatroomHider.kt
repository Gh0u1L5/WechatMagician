package com.gh0u1l5.wechatmagician.backend.plugins

import android.app.AlertDialog
import android.content.Context
import android.widget.BaseAdapter
import com.gh0u1l5.wechatmagician.Global.ITEM_ID_BUTTON_HIDE_CHATROOM
import com.gh0u1l5.wechatmagician.Global.SETTINGS_CHATTING_CHATROOM_HIDER
import com.gh0u1l5.wechatmagician.backend.WechatHook
import com.gh0u1l5.wechatmagician.backend.WechatPackage.ConversationAdapterObject
import com.gh0u1l5.wechatmagician.backend.foundation.ListViewHider
import com.gh0u1l5.wechatmagician.backend.foundation.MenuAppender
import com.gh0u1l5.wechatmagician.backend.interfaces.IAdapterHook
import com.gh0u1l5.wechatmagician.backend.interfaces.IPopupMenuHook
import com.gh0u1l5.wechatmagician.backend.interfaces.ISearchBarConsole
import com.gh0u1l5.wechatmagician.frontend.wechat.ConversationAdapter
import com.gh0u1l5.wechatmagician.storage.LocalizedStrings
import com.gh0u1l5.wechatmagician.storage.LocalizedStrings.BUTTON_HIDE_CHATROOM
import com.gh0u1l5.wechatmagician.storage.list.ChatroomHideList
import de.robv.android.xposed.XposedHelpers.getObjectField

object ChatroomHider : IAdapterHook, IPopupMenuHook, ISearchBarConsole {

    private val str = LocalizedStrings
    private val pref = WechatHook.settings

    private fun isPluginEnabled() = pref.getBoolean(SETTINGS_CHATTING_CHATROOM_HIDER, false)

    fun changeChatroomStatus(username: String?, hide: Boolean) {
        if (username == null) {
            return
        }
        if (hide) {
            ChatroomHideList += username
        } else {
            ChatroomHideList -= username
        }
        ConversationAdapterObject.get()?.notifyDataSetChanged()
    }

    override fun onConversationAdapterCreated(adapter: BaseAdapter) {
        if (!isPluginEnabled()) {
            return
        }
        ListViewHider.register(adapter, "Chatroom Hider") { item ->
            val username = getObjectField(item, "field_username")
            username in ChatroomHideList
        }
    }

    override fun onCreatePopupMenuForConversations(username: String): MenuAppender.PopupMenuItem? {
        if (!isPluginEnabled()) {
            return null
        }
        if (!username.endsWith("@chatroom")) {
            return null
        }
        val itemId = ITEM_ID_BUTTON_HIDE_CHATROOM
        val title = str[BUTTON_HIDE_CHATROOM]
        val onClickListener = { _: Context ->
            changeChatroomStatus(username, true)
        }
        return MenuAppender.PopupMenuItem(0, itemId, 0, title, onClickListener)
    }

    override fun onHandleCommand(context: Context, command: String): Boolean {
        if (!isPluginEnabled()) {
            return false
        }
        if (command.startsWith("chatrooms")) {
            val adapter = ConversationAdapter(context)
            AlertDialog.Builder(context)
                    .setTitle("Wechat Magician")
                    .setAdapter(adapter, { _, _ -> })
                    .setNegativeButton(str[LocalizedStrings.BUTTON_CANCEL], { dialog, _ ->
                        dialog.dismiss()
                    })
                    .show()
            return true
        }
        return super.onHandleCommand(context, command)
    }
}