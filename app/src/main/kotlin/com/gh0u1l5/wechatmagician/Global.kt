package com.gh0u1l5.wechatmagician

import android.annotation.SuppressLint
import android.os.Build

object Global {
    const val SALT = "W3ch4tM4g1c14n"

    const val XPOSED_PACKAGE_NAME   = "de.robv.android.xposed.installer"
    const val WECHAT_PACKAGE_NAME   = "com.tencent.mm"
    const val MAGICIAN_PACKAGE_NAME = "com.gh0u1l5.wechatmagician"

    const val XPOSED_FILE_PROVIDER   = "$XPOSED_PACKAGE_NAME.fileprovider"
    const val MAGICIAN_FILE_PROVIDER = "$MAGICIAN_PACKAGE_NAME.files"

    @SuppressLint("SdCardPath")
    private val DATA_DIR = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) "/data/data/" else "/data/user_de/0/"
    val XPOSED_BASE_DIR = "$DATA_DIR/$XPOSED_PACKAGE_NAME/"
    val MAGICIAN_BASE_DIR = "$DATA_DIR/$MAGICIAN_PACKAGE_NAME/"

    const val ITEM_ID_BUTTON_HIDE_FRIEND   = 0x510
    const val ITEM_ID_BUTTON_HIDE_CHATROOM = 0x511
    const val ITEM_ID_BUTTON_CLEAN_UNREAD  = 0x512

    const val ACTION_REQUIRE_HOOK_STATUS = "$MAGICIAN_PACKAGE_NAME.ACTION_REQUIRE_HOOK_STATUS"
    const val ACTION_REQUIRE_REPORTS     = "$MAGICIAN_PACKAGE_NAME.ACTION_REQUIRE_REPORTS"
    const val ACTION_UPDATE_PREF         = "$MAGICIAN_PACKAGE_NAME.ACTION_UPDATE_PREF"

    const val FOLDER_SHARED_PREFS = "shared_prefs"

    const val PREFERENCE_PROVIDER_AUTHORITY   = "com.gh0u1l5.wechatmagician.preferences"
    const val PREFERENCE_NAME_SETTINGS        = "settings"
    const val PREFERENCE_NAME_DEVELOPER       = "developer"
    const val PREFERENCE_NAME_SECRET_FRIEND   = "wechat-magician-secret-friend"
    const val PREFERENCE_NAME_HIDDEN_CHATROOM = "wechat-magician-hidden_chatroom"

    const val SETTINGS_AUTO_LOGIN                     = "settings_auto_login"
    const val SETTINGS_CHATTING_CHATROOM_HIDER        = "settings_chatting_chatroom_hider"
    const val SETTINGS_CHATTING_RECALL                = "settings_chatting_recall"
    const val SETTINGS_CHATTING_RECALL_PROMPT         = "settings_chatting_recall_prompt"
    const val SETTINGS_INTERFACE_HIDE_ICON            = "settings_interface_hide_icon"
    const val SETTINGS_MARK_ALL_AS_READ               = "settings_mark_all_as_read"
    const val SETTINGS_MODULE_LANGUAGE                = "settings_module_language"
    const val SETTINGS_SECRET_FRIEND                  = "settings_secret_friend"
    const val SETTINGS_SECRET_FRIEND_PASSWORD         = "settings_secret_friend_password"
    const val SETTINGS_SECRET_FRIEND_HIDE_OPTION      = "settings_secret_friend_hide_option"
    const val SETTINGS_SELECT_PHOTOS_LIMIT            = "settings_select_photos_limit"
    const val SETTINGS_SNS_ADBLOCK                    = "settings_sns_adblock"
    const val SETTINGS_SNS_DELETE_COMMENT             = "settings_sns_delete_comment"
    const val SETTINGS_SNS_DELETE_MOMENT              = "settings_sns_delete_moment"
    const val SETTINGS_SNS_KEYWORD_BLACKLIST          = "settings_sns_keyword_blacklist"
    const val SETTINGS_SNS_KEYWORD_BLACKLIST_CONTENT  = "settings_sns_keyword_blacklist_content"

    const val DEVELOPER_UI_TOUCH_EVENT      = "developer_ui_touch_event"
    const val DEVELOPER_UI_TRACE_ACTIVITIES = "developer_ui_trace_activities"
    const val DEVELOPER_UI_DUMP_POPUP_MENU  = "developer_ui_dump_popup_menu"
    const val DEVELOPER_DATABASE_QUERY      = "developer_database_query"
    const val DEVELOPER_DATABASE_INSERT     = "developer_database_insert"
    const val DEVELOPER_DATABASE_UPDATE     = "developer_database_update"
    const val DEVELOPER_DATABASE_DELETE     = "developer_database_delete"
    const val DEVELOPER_DATABASE_EXECUTE    = "developer_database_execute"
    const val DEVELOPER_TRACE_LOGCAT        = "developer_trace_logcat"
    const val DEVELOPER_TRACE_FILES         = "developer_trace_files"
    const val DEVELOPER_XML_PARSER          = "developer_xml_parser"

    val PREFERENCE_STRING_LIST_KEYS         = listOf(SETTINGS_SNS_KEYWORD_BLACKLIST_CONTENT)
}