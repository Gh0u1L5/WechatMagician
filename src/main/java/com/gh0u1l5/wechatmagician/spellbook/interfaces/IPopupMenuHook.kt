package com.gh0u1l5.wechatmagician.spellbook.interfaces

import com.gh0u1l5.wechatmagician.spellbook.hookers.MenuAppender

interface IPopupMenuHook {

    fun onPopupMenuForContactsCreating(username: String): MenuAppender.PopupMenuItem? = null

    fun onPopupMenuForConversationsCreating(username: String): MenuAppender.PopupMenuItem? = null
}