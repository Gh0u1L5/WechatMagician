package com.gh0u1l5.wechatmagician.storage

import de.robv.android.xposed.XposedBridge.log

// Strings describes the localized strings used by the module.
// NOTE: we use this object instead of Android default localized resources
// to support devices that cannot use resource hooks.
object Strings {

    val TITLE_SECRET_FRIEND           = "title_secret_friend"
    val PROMPT_SECRET_FRIEND_UNHIDE   = "prompt_secret_friend_unhide"
    val MENU_SNS_FORWARD              = "menu_sns_forward"
    val MENU_SNS_SCREENSHOT           = "menu_sns_screenshot"
    val PROMPT_WAIT                   = "prompt_wait"
    val PROMPT_SCREENSHOT             = "prompt_screenshot"
    val PROMPT_SNS_INVALID            = "prompt_sns_invalid"
    val PROMPT_RECALL                 = "prompt_recall"
    val LABEL_DELETED                 = "label_deleted"
    val BUTTON_SELECT_ALL             = "button_select_all"

    @Volatile var language: String = "zh"

    private val resources: Map<String, Map<String, String>> = mapOf(
            "zh" to mapOf(
                    TITLE_SECRET_FRIEND           to "密友",
                    PROMPT_SECRET_FRIEND_UNHIDE   to "请输入密友解锁密码",
                    MENU_SNS_FORWARD              to "转发",
                    MENU_SNS_SCREENSHOT           to "截图",
                    PROMPT_WAIT                   to "请稍等片刻……",
                    PROMPT_SCREENSHOT             to "截图已保存至 ",
                    PROMPT_SNS_INVALID            to "数据失效或已删除",
                    PROMPT_RECALL                 to "妄图撤回一条消息，啧啧",
                    LABEL_DELETED                 to "[已删除]",
                    BUTTON_SELECT_ALL             to "全选"
            ),
            "en" to mapOf(
                    TITLE_SECRET_FRIEND           to "Secret Friends",
                    PROMPT_SECRET_FRIEND_UNHIDE   to "Please enter the password to unhide secret friend",
                    MENU_SNS_FORWARD              to "Forward",
                    MENU_SNS_SCREENSHOT           to "Screenshot",
                    PROMPT_WAIT                   to "Please wait for a while......",
                    PROMPT_SCREENSHOT             to "The screenshot has been saved to ",
                    PROMPT_SNS_INVALID            to "Record is invalid or deleted.",
                    PROMPT_RECALL                 to "want to recall the message, idiot.",
                    LABEL_DELETED                 to "[Deleted]",
                    BUTTON_SELECT_ALL             to "All"
            )
    )

    operator fun get(key: String): String {
        val value = (resources[language] ?: resources["zh"])!![key]
        if (value == null) {
            log("RES => Unknown resource: $key")
            return "???"
        }
        return value
    }
}