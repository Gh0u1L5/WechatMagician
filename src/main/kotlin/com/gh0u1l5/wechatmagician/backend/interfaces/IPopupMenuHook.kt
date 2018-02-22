package com.gh0u1l5.wechatmagician.backend.interfaces

import com.gh0u1l5.wechatmagician.backend.foundation.MenuAppender

interface IPopupMenuHook {
    fun onCreatePopupMenuForContacts(username: String): MenuAppender.PopupMenuItem? = null
    fun onCreatePopupMenuForConversations(username: String): MenuAppender.PopupMenuItem? = null
}