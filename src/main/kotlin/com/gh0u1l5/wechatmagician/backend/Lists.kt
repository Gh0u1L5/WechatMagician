package com.gh0u1l5.wechatmagician.backend

import com.gh0u1l5.wechatmagician.backend.foundation.*
import com.gh0u1l5.wechatmagician.backend.foundation.base.Debug
import com.gh0u1l5.wechatmagician.backend.plugins.*

val HookList: List<() -> Unit> = listOf(
        ListViewHider::hijackMMBaseAdapter,

        MenuAppender::hijackPopupMenuEvents,
        MenuAppender::hijackPopupMenuForContacts,
        MenuAppender::hijackPopupMenuForConversations,

        Storage::hookMsgStorage,
        Storage::hookImgStorage,
        Storage::hookFileStorage,

        // TODO: refactor plugin Limits in a better way.
        Limits::breakSelectContactLimit,
        Limits::breakSelectConversationLimit,

        Activities::hookEvents,
        Adapters::hookEvents,
        Database::hookEvents,
        SearchBar::hookEvents,
        UriRouter::hookEvents,
        XmlParser::hookEvents,

        Debug::traceTouchEvents,
        Debug::traceActivities,
        Debug::dumpPopupMenu,
        Debug::traceDatabase,
        Debug::traceLogCat,
        Debug::traceFiles,
        Debug::traceXMLParse
)

val PluginList: List<Any> = listOf(
        AdBlock,
        AntiRevoke,
        AntiSnsDelete,
        AutoLogin,
        ChatroomHider,
        Donate,
        Limits,
        ObjectsHunter,
        MarkAllAsRead,
        SecretFriend,
        SnsBlock,
        SnsForward
)