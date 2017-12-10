package com.gh0u1l5.wechatmagician.storage

import de.robv.android.xposed.XposedBridge.log

// LocalizedStrings describes the localized strings used by the module.
// NOTE: we use this object instead of Android default localized resources
// to support devices that cannot use resource hooks.
object LocalizedStrings {

    val TITLE_SECRET_FRIEND           = "title_secret_friend"
    val BUTTON_HIDE_FRIEND            = "button_hide_friend"
    val PROMPT_NEW_PASSWORD           = "prompt_new_password"
    val PROMPT_VERIFY_PASSWORD        = "prompt_verify_password"
    val PROMPT_USER_NOT_FOUND         = "prompt_user_not_found"
    val PROMPT_SET_PASSWORD           = "prompt_set_password"
    val PROMPT_CORRECT_PASSWORD       = "prompt_correct_password"
    val PROMPT_WRONG_PASSWORD         = "prompt_wrong_password"
    val MENU_SNS_FORWARD              = "menu_sns_forward"
    val MENU_SNS_SCREENSHOT           = "menu_sns_screenshot"
    val PROMPT_WAIT                   = "prompt_wait"
    val PROMPT_SCREENSHOT             = "prompt_screenshot"
    val PROMPT_SNS_INVALID            = "prompt_sns_invalid"
    val PROMPT_RECALL                 = "prompt_recall"
    val LABEL_DELETED                 = "label_deleted"
    val BUTTON_SELECT_ALL             = "button_select_all"
    val BUTTON_CLEAN_UNREAD           = "button_clean_unread"
    val BUTTON_OK                     = "button_ok"
    val BUTTON_CANCEL                 = "button_cancel"

    @Volatile var language: String = "zh"

    private val resources: Map<String, Map<String, String>> = mapOf(
            "zh" to mapOf(
                    TITLE_SECRET_FRIEND           to "密友",
                    BUTTON_HIDE_FRIEND            to "隐藏好友",
                    PROMPT_NEW_PASSWORD           to "请设定新密码",
                    PROMPT_VERIFY_PASSWORD        to "请输入解锁密码",
                    PROMPT_USER_NOT_FOUND         to "用户不存在",
                    PROMPT_SET_PASSWORD           to "请先设置密码",
                    PROMPT_CORRECT_PASSWORD       to "密码正确",
                    PROMPT_WRONG_PASSWORD         to "密码错误",
                    MENU_SNS_FORWARD              to "转发",
                    MENU_SNS_SCREENSHOT           to "截图",
                    PROMPT_WAIT                   to "请稍等片刻……",
                    PROMPT_SCREENSHOT             to "截图已保存至 ",
                    PROMPT_SNS_INVALID            to "数据失效或已删除",
                    PROMPT_RECALL                 to "妄图撤回一条消息，啧啧",
                    LABEL_DELETED                 to "[已删除]",
                    BUTTON_SELECT_ALL             to "全选",
                    BUTTON_CLEAN_UNREAD           to "清空全部未读提醒",
                    BUTTON_OK                     to "确定",
                    BUTTON_CANCEL                 to "取消"
            ),
            "en" to mapOf(
                    TITLE_SECRET_FRIEND           to "Secret Friends",
                    BUTTON_HIDE_FRIEND            to "Hide This Friend",
                    PROMPT_NEW_PASSWORD           to "Please enter a new password:",
                    PROMPT_VERIFY_PASSWORD        to "Please enter your password:",
                    PROMPT_USER_NOT_FOUND         to "User Not Found!",
                    PROMPT_SET_PASSWORD           to "Please set password first!",
                    PROMPT_CORRECT_PASSWORD       to "Correct Password!",
                    PROMPT_WRONG_PASSWORD         to "Wrong Password!",
                    MENU_SNS_FORWARD              to "Forward",
                    MENU_SNS_SCREENSHOT           to "Screenshot",
                    PROMPT_WAIT                   to "Please wait for a while......",
                    PROMPT_SCREENSHOT             to "The screenshot has been saved to ",
                    PROMPT_SNS_INVALID            to "Record is invalid or deleted.",
                    PROMPT_RECALL                 to "want to recall the message, idiot.",
                    LABEL_DELETED                 to "[Deleted]",
                    BUTTON_SELECT_ALL             to "All",
                    BUTTON_CLEAN_UNREAD           to "Mark All as Read",
                    BUTTON_OK                     to "Okay",
                    BUTTON_CANCEL                 to "Cancel"
            )
    )

    operator fun get(key: String): String {
        val res = resources[language] ?: resources["zh"]
        val value = res!![key]
        if (value == null) {
            log("RES => Resource Missing: $key")
            return "???"
        }
        return value
    }
}