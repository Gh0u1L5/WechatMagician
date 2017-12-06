package com.gh0u1l5.wechatmagician

object Global {
    val LOG_TAG = "WechatMagician"

    val SALT = "W3ch4tM4g1c14n"
    val SHA_MAGIC_STRING = "######SHA256######"

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

    val ACTION_UPDATE_PREF = "$MAGICIAN_PACKAGE_NAME.ACTION_UPDATE_PREF"
    val PREFERENCE_STRING_LIST_KEYS = listOf("settings_sns_keyword_blacklist_content")

    val PREFERENCE_NAME_SETTINGS      = "settings"
    val PREFERENCE_NAME_DEVELOPER     = "developer"
    val PREFERENCE_NAME_SECRET_FRIEND = "wechat-magician-secret-friend"
}