package com.gh0u1l5.wechatmagician.backend.storage

import com.gh0u1l5.wechatmagician.Global.SETTINGS_MODULE_LANGUAGE
import com.gh0u1l5.wechatmagician.R
import com.gh0u1l5.wechatmagician.backend.WechatHook
import de.robv.android.xposed.XposedBridge.log
import java.util.*

object Strings {

    private val pref = WechatHook.settings
    @Volatile var language: String = Locale.getDefault().language

    // Use hard coded strings if the module cannot load resources from its APK
    private val hardcodedStrings: Map<String, Map<Int, String>> = mapOf (
            // NOTE: Replace <string name="(.*?)">(.*?)</string> to R.string.$1 to "$2",
            "en" to mapOf (
                    R.string.button_ok to "Okay",
                    R.string.button_update to "Update",
                    R.string.button_cancel to "Cancel",
                    R.string.button_clean_unread to "Mark All as Read",
                    R.string.button_hide_chatroom to "Hide Useless Chatroom",
                    R.string.button_unhide_chatroom to "Unhide Chatroom",
                    R.string.button_hide_friend to "Hide Friend",
                    R.string.button_select_all to "All",
                    R.string.button_sns_forward to "Forward",
                    R.string.button_sns_screenshot to "Screenshot",

                    R.string.prompt_alipay_not_found to "Alipay Not Found",
                    R.string.prompt_load_component_status_failed to "Failed to retrieve the status of components.",
                    R.string.prompt_message_recall to "want to recall the message, idiot.",
                    R.string.prompt_need_reboot to "Take effect after reboot.",
                    R.string.prompt_screenshot to "The screenshot has been saved to",
                    R.string.prompt_sns_invalid to "Record is invalid or deleted.",
                    R.string.prompt_update_discovered to "Update Discovered",
                    R.string.prompt_wait to "Please wait for a while.....",

                    R.string.prompt_setup_password to "Enter a new password",
                    R.string.prompt_verify_password to "Enter your password",
                    R.string.prompt_user_not_found to "User Not Found!",
                    R.string.prompt_password_missing to "Please set your password first!",
                    R.string.prompt_correct_password to "Correct Password!",
                    R.string.prompt_wrong_password to "Wrong Password!",

                    R.string.label_deleted to "[Deleted]",
                    R.string.label_unnamed to "[Unnamed]",
                    R.string.title_secret_friend to "Secret Friend"
            ),
            "zh" to mapOf (
                    R.string.button_ok to "确定",
                    R.string.button_update to "更新",
                    R.string.button_cancel to "取消",
                    R.string.button_clean_unread to "清空全部未读提醒",
                    R.string.button_hide_chatroom to "隐藏无用群聊",
                    R.string.button_unhide_chatroom to "还原群聊",
                    R.string.button_hide_friend to "隐藏好友",
                    R.string.button_select_all to "全选",
                    R.string.button_sns_forward to "转发",
                    R.string.button_sns_screenshot to "截图",

                    R.string.prompt_alipay_not_found to "未发现支付宝手机客户端",
                    R.string.prompt_load_component_status_failed to "组件状态检查失败",
                    R.string.prompt_message_recall to "妄图撤回一条消息，啧啧",
                    R.string.prompt_need_reboot to "重启后生效",
                    R.string.prompt_screenshot to "截图已保存至",
                    R.string.prompt_sns_invalid to "数据失效或已删除",
                    R.string.prompt_update_discovered to "发现更新",
                    R.string.prompt_wait to "请稍候片刻……",

                    R.string.prompt_setup_password to "请设定新密码",
                    R.string.prompt_verify_password to "请输入解锁密码",
                    R.string.prompt_user_not_found to "用户不存在",
                    R.string.prompt_password_missing to "请先设置密码",
                    R.string.prompt_correct_password to "密码正确",
                    R.string.prompt_wrong_password to "密码错误",

                    R.string.label_deleted to "[已删除]",
                    R.string.label_unnamed to "[未命名]",
                    R.string.title_secret_friend to "密友"
            )
    )

    fun getString(id: Int): String {
        val resources = WechatHook.resources
        if (resources != null) {
            return resources.getString(id)
        }
        val language = pref.getString(SETTINGS_MODULE_LANGUAGE, language)
        if (language !in hardcodedStrings) {
            log("Unknown Language: $language")
        }
        val strings = hardcodedStrings[language] ?: hardcodedStrings["en"]
        if (id !in strings!!) {
            log("Unknown String ID: $id")
        }
        return strings[id] ?: "???"
    }
}