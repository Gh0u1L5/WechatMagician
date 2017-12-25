package com.gh0u1l5.wechatmagician

object Global {
    val SALT = "W3ch4tM4g1c14n"
    val LOG_TAG = "WechatMagician"

    val XPOSED_PACKAGE_NAME   = "de.robv.android.xposed.installer"
    val WECHAT_PACKAGE_NAME   = "com.tencent.mm"
    val MAGICIAN_PACKAGE_NAME = "com.gh0u1l5.wechatmagician"

    val FOLDER_SHARED = "shared"
    val FOLDER_SHARED_PREFS = "shared_prefs"

    val STATUS_FLAG_MSG_STORAGE = "MsgStorage"
    val STATUS_FLAG_IMG_STORAGE = "ImgStorage"
    val STATUS_FLAG_RESOURCES   = "Resources"
    val STATUS_FLAG_DATABASE    = "Database"
    val STATUS_FLAG_XML_PARSER  = "XMLParser"
    val STATUS_FLAG_URI_ROUTER  = "UriRouter"
    val STATUS_FLAG_COMMAND     = "SearchBarCommand"

    val PREFERENCE_NAME_SETTINGS      = "settings"
    val PREFERENCE_NAME_DEVELOPER     = "developer"
    val PREFERENCE_NAME_SECRET_FRIEND = "wechat-magician-secret-friend"

    val SETTINGS_AUTO_LOGIN                     = "settings_auto_login"
    val SETTINGS_CHATTING_RECALL                = "settings_chatting_recall"
    val SETTINGS_CHATTING_RECALL_PROMPT         = "settings_chatting_recall_prompt"
    val SETTINGS_INTERFACE_HIDE_ICON            = "settings_interface_hide_icon"
    val SETTINGS_MODULE_LANGUAGE                = "settings_module_language"
    val SETTINGS_SECRET_FRIEND                  = "settings_secret_friend"
    val SETTINGS_SECRET_FRIEND_PASSWORD         = "settings_secret_friend_password"
    val SETTINGS_SELECT_PHOTOS_LIMIT            = "settings_select_photos_limit"
    val SETTINGS_SNS_DELETE_COMMENT             = "settings_sns_delete_comment"
    val SETTINGS_SNS_DELETE_MOMENT              = "settings_sns_delete_moment"
    val SETTINGS_SNS_KEYWORD_BLACKLIST          = "settings_sns_keyword_blacklist"
    val SETTINGS_SNS_KEYWORD_BLACKLIST_CONTENT  = "settings_sns_keyword_blacklist_content"

    val DEVELOPER_UI_TOUCH_EVENT      = "developer_ui_touch_event"
    val DEVELOPER_UI_TRACE_ACTIVITIES = "developer_ui_trace_activities"
    val DEVELOPER_UI_DUMP_POPUP_MENU  = "developer_ui_dump_popup_menu"
    val DEVELOPER_UI_XLOG             = "developer_ui_xlog"
    val DEVELOPER_DATABASE_QUERY      = "developer_database_query"
    val DEVELOPER_DATABASE_INSERT     = "developer_database_insert"
    val DEVELOPER_DATABASE_UPDATE     = "developer_database_update"
    val DEVELOPER_DATABASE_DELETE     = "developer_database_delete"
    val DEVELOPER_DATABASE_EXECUTE    = "developer_database_execute"
    val DEVELOPER_TRACE_LOGCAT        = "developer_trace_logcat"
    val DEVELOPER_XML_PARSER          = "developer_xml_parser"

    val ACTION_UPDATE_PREF = "$MAGICIAN_PACKAGE_NAME.ACTION_UPDATE_PREF"
    val PREFERENCE_STRING_LIST_KEYS = listOf(SETTINGS_SNS_KEYWORD_BLACKLIST_CONTENT)

    val ITEM_ID_BUTTON_HIDE_FRIEND  = 0x510
    val ITEM_ID_BUTTON_CLEAN_UNREAD = 0x511
}