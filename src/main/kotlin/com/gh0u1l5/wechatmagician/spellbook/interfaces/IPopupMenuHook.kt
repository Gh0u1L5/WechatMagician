package com.gh0u1l5.wechatmagician.spellbook.interfaces

import com.gh0u1l5.wechatmagician.spellbook.hookers.MenuAppender

interface IPopupMenuHook {
    fun onCreatePopupMenuForContacts(username: String): MenuAppender.PopupMenuItem? = null
    fun onCreatePopupMenuForConversations(username: String): MenuAppender.PopupMenuItem? = null
}