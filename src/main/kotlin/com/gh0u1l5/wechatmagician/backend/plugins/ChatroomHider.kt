package com.gh0u1l5.wechatmagician.backend.plugins

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.Gravity
import android.view.View
import android.widget.BaseAdapter
import android.widget.ListPopupWindow
import com.gh0u1l5.wechatmagician.Global.ITEM_ID_BUTTON_HIDE_CHATROOM
import com.gh0u1l5.wechatmagician.Global.SETTINGS_CHATTING_CHATROOM_HIDER
import com.gh0u1l5.wechatmagician.backend.WechatHook
import com.gh0u1l5.wechatmagician.backend.storage.LocalizedStrings
import com.gh0u1l5.wechatmagician.backend.storage.LocalizedStrings.BUTTON_CANCEL
import com.gh0u1l5.wechatmagician.backend.storage.LocalizedStrings.BUTTON_HIDE_CHATROOM
import com.gh0u1l5.wechatmagician.backend.storage.LocalizedStrings.MENU_CHATROOM_UNHIDE
import com.gh0u1l5.wechatmagician.backend.storage.list.ChatroomHideList
import com.gh0u1l5.wechatmagician.frontend.wechat.ConversationAdapter
import com.gh0u1l5.wechatmagician.frontend.wechat.StringListAdapter
import com.gh0u1l5.wechatmagician.spellbook.WechatPackage
import com.gh0u1l5.wechatmagician.spellbook.WechatPackage.ConversationAdapterObject
import com.gh0u1l5.wechatmagician.spellbook.hookers.ListViewHider
import com.gh0u1l5.wechatmagician.spellbook.hookers.MenuAppender
import com.gh0u1l5.wechatmagician.spellbook.interfaces.IAdapterHook
import com.gh0u1l5.wechatmagician.spellbook.interfaces.IPopupMenuHook
import com.gh0u1l5.wechatmagician.spellbook.interfaces.ISearchBarConsole
import com.gh0u1l5.wechatmagician.util.ViewUtil.dp2px
import de.robv.android.xposed.XposedHelpers.getObjectField

object ChatroomHider : IAdapterHook, IPopupMenuHook, ISearchBarConsole {

    private val str = LocalizedStrings
    private val pref = WechatHook.settings

    private fun isPluginEnabled() = pref.getBoolean(SETTINGS_CHATTING_CHATROOM_HIDER, false)

    private fun changeChatroomStatus(username: String?, hide: Boolean) {
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

    override fun onPopupMenuForConversationsCreating(username: String): MenuAppender.PopupMenuItem? {
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
                    .setNegativeButton(str[BUTTON_CANCEL], { dialog, _ ->
                        dialog.dismiss()
                    })
                    .show()
            return true
        }
        return super.onHandleCommand(context, command)
    }

    fun onChatroomHiderConversationClick(view: View, username: String): Boolean {
        view.context.startActivity(Intent(view.context, WechatPackage.ChattingUI)
                .putExtra("Chat_Mode", 1)
                .putExtra("Chat_User", username))
        return true
    }

    fun onChatroomHiderConversationLongClick(view: View, adapter: ConversationAdapter, username: String): Boolean {
        val operations = listOf(str[MENU_CHATROOM_UNHIDE])
        ListPopupWindow(view.context).apply {
            anchorView = view
            width = view.context.dp2px(140)
            setDropDownGravity(Gravity.CENTER)
            setAdapter(StringListAdapter(view.context, operations))
            setOnItemClickListener { _, _, operation, _ ->
                onChatroomHiderConversationPopupMenuSelected(adapter, username, operation)
                dismiss()
            }
        }.show()
        return true
    }

    private fun onChatroomHiderConversationPopupMenuSelected(adapter: ConversationAdapter, username: String, operation: Int): Boolean {
        when (operation) {
            0 -> { // Unhide
                changeChatroomStatus(username, false)
                adapter.conversations.clear()
                adapter.conversations.addAll(ConversationAdapter.getConversationList())
                adapter.notifyDataSetChanged()
                return true
            }
        }
        return false
    }
}