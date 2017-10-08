package com.gh0u1l5.wechatmagician.storage

import de.robv.android.xposed.XposedBridge.log

// LocalizedResources describes the localized resources used by the module.
object LocalizedResources {

    @Volatile var language: String = "zh"

    private val resources: Map<String, Map<String, String>> = mapOf(
            "zh" to mapOf(
                    "menu_sns_forward"    to "转发",
                    "menu_sns_screenshot" to "截图",
                    "prompt_wait"         to "请稍等片刻……",
                    "prompt_screenshot"   to "截图已保存至 ",
                    "prompt_sns_invalid"  to "数据失效或已删除",
                    "label_easter_egg"    to "妄图撤回一条消息，啧啧",
                    "label_deleted"       to "[已删除]",
                    "button_select_all"   to "全选"
            ),
            "en" to mapOf(
                    "menu_sns_forward"    to "Forward",
                    "menu_sns_screenshot" to "Screenshot",
                    "prompt_wait"         to "Please wait for a while......",
                    "prompt_screenshot"   to "The screenshot has been saved to ",
                    "prompt_sns_invalid"  to "Record is invalid or deleted.",
                    "label_easter_egg"    to "want to recall the message, idiot.",
                    "label_deleted"       to "[Deleted]",
                    "button_select_all"   to "All"
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