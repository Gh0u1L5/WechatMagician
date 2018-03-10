package com.gh0u1l5.wechatmagician.backend.plugins

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.gh0u1l5.wechatmagician.Global.ITEM_ID_BUTTON_CLEAN_UNREAD
import com.gh0u1l5.wechatmagician.Global.SETTINGS_MARK_ALL_AS_READ
import com.gh0u1l5.wechatmagician.backend.WechatHook
import com.gh0u1l5.wechatmagician.backend.storage.LocalizedStrings
import com.gh0u1l5.wechatmagician.backend.storage.LocalizedStrings.BUTTON_CLEAN_UNREAD
import com.gh0u1l5.wechatmagician.backend.storage.database.MainDatabase
import com.gh0u1l5.wechatmagician.spellbook.WechatPackage.LauncherUI
import com.gh0u1l5.wechatmagician.spellbook.hookers.MenuAppender
import com.gh0u1l5.wechatmagician.spellbook.interfaces.IPopupMenuHook
import me.leolin.shortcutbadger.ShortcutBadger

object MarkAllAsRead : IPopupMenuHook {

    private val str = LocalizedStrings
    private val pref = WechatHook.settings

    private fun isPluginEnabled() = pref.getBoolean(SETTINGS_MARK_ALL_AS_READ, true)

    private fun cleanUnreadCount(activity: Activity?) {
        if (activity == null) {
            return
        }

        MainDatabase.cleanUnreadCount()
        ShortcutBadger.removeCount(activity)
        activity.finish()
        activity.startActivity(Intent(activity, LauncherUI).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME)
        })
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun onPopupMenuForConversationsCreating(username: String): MenuAppender.PopupMenuItem? {
        if (!isPluginEnabled()) {
            return null
        }
        val itemId = ITEM_ID_BUTTON_CLEAN_UNREAD
        val title = str[BUTTON_CLEAN_UNREAD]
        val onClickListener = { context: Context ->
            cleanUnreadCount(context as Activity)
        }
        return MenuAppender.PopupMenuItem(0, itemId, 0, title, onClickListener)
    }
}