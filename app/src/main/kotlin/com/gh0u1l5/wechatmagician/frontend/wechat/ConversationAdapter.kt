package com.gh0u1l5.wechatmagician.frontend.wechat

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import com.gh0u1l5.wechatmagician.R
import com.gh0u1l5.wechatmagician.backend.plugins.ChatroomHider
import com.gh0u1l5.wechatmagician.backend.storage.Strings
import com.gh0u1l5.wechatmagician.backend.storage.database.MainDatabase
import com.gh0u1l5.wechatmagician.backend.storage.list.ChatroomHideList
import com.gh0u1l5.wechatmagician.util.ViewUtil.dp2px
import de.robv.android.xposed.XposedHelpers

class ConversationAdapter(context: Context, val conversations: MutableList<ConversationAdapter.Conversation> = getConversationList()) :
        ArrayAdapter<ConversationAdapter.Conversation>(context, 0, conversations) {

    data class Conversation(
            val username: String,
            val nickname: String,
            val digest: String,
            val digestUser: String,
            val atCount: Int,
            val unreadCount: Int
    )

    companion object {
        fun getConversationList(): MutableList<Conversation> {
            return ChatroomHideList.toList().mapNotNull {
                val contact = MainDatabase.getContactByUsername(username = it)
                val conversation = MainDatabase.getConversationByUsername(username = it)
                if (contact != null && conversation != null) {
                    ConversationAdapter.Conversation(
                            username    = contact.username,
                            nickname    = contact.nickname,
                            digest      = conversation.digest,
                            digestUser  = conversation.digestUser,
                            atCount     = conversation.atCount,
                            unreadCount = conversation.unreadCount
                    )
                } else null
            }.toMutableList()
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView as LinearLayout?
        if (view == null) {
            val containerLayout = XposedHelpers.callMethod(parent, "generateDefaultLayoutParams")
            XposedHelpers.setIntField(containerLayout, "height", context.dp2px(60))
            view = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                if (containerLayout is ViewGroup.LayoutParams) {
                    layoutParams = containerLayout
                }
                setBackgroundColor(Color.WHITE)

                val textLayout = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT)
                textLayout.leftMargin  = context.dp2px(15)
                textLayout.rightMargin = context.dp2px(15)
                textLayout.topMargin   = context.dp2px(3)
                textLayout.weight = 1F

                // Add nickname row
                addView(TextView(context).apply {
                    textSize = 16F
                    gravity = Gravity.CENTER_VERTICAL
                    setSingleLine()
                    setTextColor(Color.BLACK)
                }, textLayout)

                // Add digest row
                addView(TextView(context).apply {
                    textSize = 12F
                    gravity = Gravity.TOP
                    setSingleLine()
                    setTextColor(Color.GRAY)
                }, textLayout)
            }
        }
        return view.apply {
            val conversation = getItem(position)
            val nickname = getChildAt(0) as TextView?
            val digest = getChildAt(1) as TextView?
            if (conversation.nickname.isEmpty()) {
                nickname?.text = Strings.getString(R.string.label_unnamed)
            } else {
                nickname?.text = conversation.nickname
            }
            digest?.text = conversation.digest
                    .format(conversation.digestUser)
                    .replace("\n", "")
            setOnClickListener { view ->
                ChatroomHider.onChatroomHiderConversationClick(view, conversation.username)
            }
            setOnLongClickListener { view ->
                ChatroomHider.onChatroomHiderConversationLongClick(view, this@ConversationAdapter, conversation.username)
            }
        }
    }
}