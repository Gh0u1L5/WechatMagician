package com.gh0u1l5.wechatmagician.spellbook.mirror.mm.ui.conversation

import com.gh0u1l5.wechatmagician.spellbook.C
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxClasses
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxLazy
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxLoader
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxPackageName
import com.gh0u1l5.wechatmagician.spellbook.WechatGlobal.wxVersion
import com.gh0u1l5.wechatmagician.spellbook.base.Version
import com.gh0u1l5.wechatmagician.spellbook.util.ReflectionUtil.findClassesFromPackage

object Classes {
    private val classesInCurrentPackage by wxLazy("$wxPackageName.ui.conversation") {
        findClassesFromPackage(wxLoader!!, wxClasses!!, "$wxPackageName.ui.conversation")
    }

    val ConversationWithCacheAdapter: Class<*> by wxLazy("ConversationWithCacheAdapter") {
        classesInCurrentPackage
                .filterByMethod(null, "clearCache")
                .firstOrNull()
    }

    val ConversationCreateContextMenuListener: Class<*> by wxLazy("ConversationCreateContextMenuListener") {
        when {
            wxVersion!! >= Version("6.5.8") -> ConversationLongClickListener
            else -> MainUI
        }
    }

    val ConversationLongClickListener: Class<*> by wxLazy("ConversationLongClickListener") {
        when {
            wxVersion!! >= Version("6.5.8") ->
                classesInCurrentPackage
                        .filterByMethod(null, "onCreateContextMenu", C.ContextMenu, C.View, C.ContextMenuInfo)
                        .filterByMethod(C.Boolean, "onItemLongClick", C.AdapterView, C.View, C.Int, C.Long)
                        .firstOrNull()
            else ->
                classesInCurrentPackage
                        .filterByEnclosingClass(MainUI)
                        .filterByMethod(C.Boolean, "onItemLongClick", C.AdapterView, C.View, C.Int, C.Long)
                        .firstOrNull()
        }
    }

    val MainUI: Class<*> by wxLazy("MainUI") {
        classesInCurrentPackage
                .filterByMethod(C.Int, "getLayoutId")
                .filterByMethod(null, "onConfigurationChanged", C.Configuration)
                .firstOrNull()
    }
}