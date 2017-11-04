package com.gh0u1l5.wechatmagician

object Global {
    val LOG_TAG = "WechatMagician"

    val XPOSED_PACKAGE_NAME   = "de.robv.android.xposed.installer"
    val WECHAT_PACKAGE_NAME   = "com.tencent.mm"
    val MAGICIAN_PACKAGE_NAME = "com.gh0u1l5.wechatmagician"

    val STATUS_FLAG_HOOKING       = "Hooking"
    val STATUS_FLAG_MSG_STORAGE   = "MsgStorage"
    val STATUS_FLAG_IMG_STORAGE   = "ImgStorage"
    val STATUS_FLAG_RESOURCES     = "Resources"
    val STATUS_FLAG_DATABASE      = "Database"
    val STATUS_FLAG_XML_PARSER    = "XMLParser"
    val STATUS_FLAG_CUSTOM_SCHEME = "CustomScheme"

    val INTENT_PREF_KEYS = "preference_keys"
    val INTENT_PREF_NAME = "preference_name"
    val INTENT_STATUS_FIELDS = "status_fields"

    val ACTION_DESIRE_PREF = "$MAGICIAN_PACKAGE_NAME.ACTION_DESIRE_PREF"
    val ACTION_UPDATE_PREF = "$MAGICIAN_PACKAGE_NAME.ACTION_UPDATE_PREF"
    val ACTION_REQUIRE_STATUS = "$MAGICIAN_PACKAGE_NAME.ACTION_REQUIRE_STATUS"

    val PREFERENCE_STRING_LIST_KEYS = setOf("settings_sns_keyword_blacklist_content")
}